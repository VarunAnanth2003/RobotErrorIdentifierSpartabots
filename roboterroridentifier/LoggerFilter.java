package roboterroridentifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * A client that can be run at the end of matches to parse through .dsevents
 * files and output a ".txt" file that only contains important information about
 * robot malfunctions. It can also parse further through the use of commands to
 * find specific errors. Helpful for post-match diagnostics.
 * 
 * @version 6.0.0
 * @author Team 2976!
 */
public class LoggerFilter {
    /**
     * Location of all .dsevents files, filepath can be found through Driver Station
     * Log File Viewer. READ THROUGH "config.txt". Do not paste here.
     */
    private static String folderPath = "";
    /**
     * Filename to parse. Do not edit!
     */
    public static String fileName = "";
    /**
     * Whole path to file.
     */
    private static String wholePath = folderPath + fileName;
    /**
     * Upper bound to use when parsing for errors.
     */
    private static final String ALERT_KEY_UPPER_BOUND = "S_LOG";
    /**
     * Lower bound to use when parsing for errors.
     */
    private static final String ALERT_KEY_LOWER_BOUND = "E_LOG";
    /**
     * Heads of PrintStyles.
     */
    public static final String[] MESSAGE_HEADS = { "###", "<<< Warning:", "!!! Error:", "<P><P><P> Sensor Reading:" };
    /**
     * Ends of PrintStyles.
     */
    public static final String[] MESSAGE_ENDS = { "###", ">>>", "!!!", "<P><P><P>" };
    /**
     * Types of PrintStyles (Correlates with MESSAGE_HEADS and MESSAGE_ENDS).
     */
    public static final String[] TYPE_KEYS = { "Message", "Warning", "Error", "Sensor Data" };
    /**
     * Subsystem name keywords. READ THROUGH "config.txt". Do not input manually.
     */
    public static String[] SUBSYSTEM_KEYS;
    /**
     * Actuator name keywords.
     */
    public static ArrayList<String> ACTUATOR_NAMES = new ArrayList<String>();
    /**
     * An ArrayList<String> to store errors in order, with only one occurence of
     * each.
     */
    public static ArrayList<String> KEYS_IN_ORDER = new ArrayList<>();
    /**
     * All text from the .dsevents file.
     */
    private static String allText = "";
    /**
     * A LogList of all logs, unedited and unparsed.
     */
    private static LogList allLogs = new LogList();
    /**
     * An ArrayList<LogList> of LogLists that contains LogLists of a different
     * PrintStyle in each index.
     */
    private static ArrayList<LogList> typeLogs = new ArrayList<>();
    /**
     * An ArrayList<LogList> of LogLists that contains LogLists of a different
     * subsystem in each index.
     */
    private static ArrayList<LogList> subsystemLogs = new ArrayList<>();
    /**
     * A LogList that has temporary data written to it for compounding.
     */
    private static LogList toParse = new LogList();
    /**
     * The boolean that controls the state of compounding.
     */
    private static boolean compounding = false;
    /**
     * How many lines the console can handle. READ THROUGH "config.txt". Do not edit
     * manually.
     */
    public static int overflowLineMax;

    /**
     * Executes the logger!
     */
    public static void executeLogger() {
        resetClassVars();
        readFile();
    }

    /**
     * Resets all class variables to their instantiation states in case the output
     * needs to be regenerated.
     */
    private static void resetClassVars() {
        KEYS_IN_ORDER = new ArrayList<>();
        allText = "";
        allLogs = new LogList();
        typeLogs = new ArrayList<>();
        subsystemLogs = new ArrayList<>();
        ACTUATOR_NAMES = new ArrayList<>();
        toParse = new LogList();
        compounding = false;
    }

    /**
     * Gets the config settings for the project. These are found in "config.txt".
     * Creates this file if it does not already exist.
     */
    public static void getConfig() {
        try {
            if (!new File("config.txt").exists()) {
                final FileWriter fw = new FileWriter("config.txt", false);
                final PrintWriter printer = new PrintWriter(fw);
                printer.println(".dsevents folder path: N/A");
                printer.println("All subsystem keyword names (comma separated): {N/A}");
                printer.close();
            }
            final Scanner sc = new Scanner(new File("config.txt"));
            final String[] allLines = new String[3];
            int counter = 0;
            while (sc.hasNextLine()) {
                allLines[counter] = sc.nextLine();
                counter++;
            }
            if (!allLines[0].endsWith("\\")) {
                allLines[0] = allLines[0].trim();
                allLines[0] += "\\";
            }
            allLines[0] = allLines[0].replace(".dsevents folder path: ", "");
            folderPath = allLines[0].trim();

            allLines[1] = allLines[1].substring(allLines[1].indexOf("{") + 1, allLines[1].indexOf("}"));
            final String[] keywordNames = allLines[1].split(",");
            SUBSYSTEM_KEYS = new String[keywordNames.length];
            for (int i = 0; i < keywordNames.length; i++) {
                SUBSYSTEM_KEYS[i] = keywordNames[i].trim();
            }
            try {
                allLines[2] = allLines[2].replace("Console Overflow Limit:", "").trim();
                overflowLineMax = Integer.parseInt(allLines[2]);
            } catch (final NumberFormatException e) {
                overflowLineMax = 100;
            }
            sc.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets "SUBSYSTEM_KEYS" to the information in the corresponding settings text
     * box.
     * 
     * @param s -> String from the text box.
     */
    public static void setSubsystemKeywords(String s) {
        final String[] keywordNames = s.split(",");
        SUBSYSTEM_KEYS = new String[keywordNames.length];
        for (int i = 0; i < keywordNames.length; i++) {
            SUBSYSTEM_KEYS[i] = keywordNames[i].trim();
        }
        LoggerGUI.printToFrame("Subsystem keys: " + Arrays.toString(SUBSYSTEM_KEYS));
    }

    /**
     * Sets "overflowLineMax" to the information in the corresponding settings text
     * box.
     * 
     * @param s -> String from the text box.
     */
    public static void setOverflowLimit(String s) {
        try {
            overflowLineMax = Integer.parseInt(s);
            LoggerGUI.printToFrame("Console Overflow Limit: " + overflowLineMax);
            ;
        } catch (final NumberFormatException e) {
            LoggerGUI.printToFrame("NaI inputted into Console Overflow Limit: Defaulting to 100.");
            overflowLineMax = 100;
        }
    }

    /**
     * Uses the .lastModified(); method to get the name of the most recently created
     * .dsevents file within a directory.
     */
    public static void getMostRecentFile() {
        try {
            final File directory = new File(folderPath);
            final File[] allFiles = directory.listFiles();
            long lastModTime = allFiles[0].lastModified();
            File mostRecentFile = allFiles[0];
            for (final File f : allFiles) {
                if (f.lastModified() > lastModTime) {
                    lastModTime = f.lastModified();
                    mostRecentFile = f;
                }
                fileName = mostRecentFile.getName();
                wholePath = folderPath + fileName;
            }
        } catch (final Exception e) {
            LoggerGUI.printToFrame(
                    "PATH NOT FOUND. PLEASE EDIT \"config.txt\" FOUND IN THE SAME FOLDER AS \"RobotErrorIdentifier.exe\"");
        }
    }

    /**
     * Reads the file in through BufferedReader and concatenates each readLine()
     * onto a String called "allText".
     */
    private static void readFile() {
        try {
            final FileReader fr = new FileReader(wholePath);
            final BufferedReader br = new BufferedReader(fr);
            allText = "";
            String contentLine = br.readLine();
            while (contentLine != null) {
                allText += contentLine;
                contentLine = br.readLine();
            }
            parseData(allText.replaceAll("\\|", "<P>"));
            br.close();
        } catch (final FileNotFoundException e) {
            LoggerGUI.printToFrame("Failed to find file.");
            e.printStackTrace();
        } catch (final IOException e) {
            LoggerGUI.printToFrame("Failed to read file.");
            e.printStackTrace();
        }
    }

    /**
     * Parses through the .dsevents file as a String and looks for specific error
     * messages bounded by "ALERT_KEY_UPPER_BOUND" and "ALERT_KEY_LOWER_BOUND"
     * (These variables can be found at the top of the class as class constants). It
     * then adds these parsed and filtered error messages to an ArrayList<String>
     * object. Added more!
     * 
     * @param s -> The .dsevents file as a String.
     * @throws IOException
     */
    private static void parseData(String s) throws IOException {
        for (int i = 0; i < MESSAGE_ENDS.length; i++) {
            typeLogs.add(new LogList());
        }
        for (int i = 0; i < SUBSYSTEM_KEYS.length; i++) {
            subsystemLogs.add(new LogList());
        }
        s = s.trim();
        while (s.contains(ALERT_KEY_UPPER_BOUND) && s.contains(ALERT_KEY_LOWER_BOUND)) {
            final int a = s.indexOf(ALERT_KEY_UPPER_BOUND);
            final int b = s.indexOf(ALERT_KEY_LOWER_BOUND) + ALERT_KEY_LOWER_BOUND.length();
            String logLine = s.substring(a, b);
            logLine = logLine.trim();
            s = s.replaceFirst(logLine, "");

            logLine = logLine.replaceAll(ALERT_KEY_UPPER_BOUND, "");
            logLine = logLine.replaceAll(ALERT_KEY_LOWER_BOUND, "");

            for (int i = 0; i < MESSAGE_HEADS.length; i++) {
                if (logLine.contains(MESSAGE_HEADS[i])) {
                    logLine = logLine.replaceFirst(MESSAGE_HEADS[i], "");
                    logLine = logLine.replaceFirst(MESSAGE_ENDS[i], "");
                    logLine = logLine.trim();
                    typeLogs.get(i).messages.add(logLine);
                }
            }
            for (int i = 0; i < SUBSYSTEM_KEYS.length; i++) {
                if (logLine.contains(SUBSYSTEM_KEYS[i])) {
                    subsystemLogs.get(i).messages.add(logLine);
                }
            }
            allLogs.messages.add(logLine.trim());
        }

        for (int j = 0; j < MESSAGE_HEADS.length; j++) {
            typeLogs.get(j).values = (hashify(typeLogs.get(j).messages, typeLogs.get(j).timeStamps));
        }
        for (int i = 0; i < SUBSYSTEM_KEYS.length; i++) {
            subsystemLogs.get(i).values = hashify(subsystemLogs.get(i).messages, subsystemLogs.get(i).timeStamps);
        }

        allLogs.values = hashify(allLogs.messages, allLogs.timeStamps);
        writeToFile(allLogs.values);
    }

    /**
     * Creates an array of timestamps from the array of errors, and then puts them
     * into a HashMap that contains initial timestamp, final timestamp, and
     * frequency.
     * 
     * @param errorArray         -> ArrayList<String> of errors with timestamps
     *                           included.
     * @param allLogs.timeStamps -> ArrayList<String> that timestamps will be moved
     *                           to by the end of the method.
     * @return A Hashmap with the error as a key (String), and a List<String> of
     *         initial timestamps, final timestamps, and frequencies for each error.
     */
    private static HashMap<String, List<String>> hashify(final ArrayList<String> errorArray,
            final ArrayList<String> timeStamps) {
        for (int i = 0; i < errorArray.size(); i++) {
            timeStamps.add(
                    errorArray.get(i).substring(errorArray.get(i).indexOf("<") + 1, errorArray.get(i).indexOf(">")));
            errorArray.set(i, (errorArray.get(i).replace(
                    errorArray.get(i).substring(errorArray.get(i).indexOf("<"), errorArray.get(i).indexOf(">") + 1),
                    "")).trim());
        }
        final HashMap<String, List<String>> values = new HashMap<>();
        for (String s : errorArray) {
            if (values.containsKey(s)) {
                values.get(s).set(2, "" + ((Integer.parseInt(values.get(s).get(2))) + 1));
            } else {
                values.put(s, Arrays.asList(timeStamps.get(errorArray.indexOf(s)),
                        timeStamps.get(errorArray.lastIndexOf(s)), "1"));
                if (allLogs.messages.equals(errorArray)) {
                    KEYS_IN_ORDER.add(s);
                    s = s.replaceFirst("@", "<S>");
                    s = s.replaceFirst("@", "<E>");
                    if (s.contains("<S>") && s.contains("<E>")) {
                        if (!ACTUATOR_NAMES.contains(s.substring(s.indexOf("<S>") + 3, s.indexOf("<E>"))))
                            ACTUATOR_NAMES.add(s.substring(s.indexOf("<S>") + 3, s.indexOf("<E>")));
                    }
                }
            }
        }
        return values;
    }

    /**
     * Creates a String[] from KEYS_IN_ORDER.
     * 
     * @return A String array that has the same elements of KEYS_IN_ORDER
     */
    public static String[] getErrors() {
        final String[] errorArr = new String[KEYS_IN_ORDER.size()];
        for (int i = 0; i < KEYS_IN_ORDER.size(); i++) {
            errorArr[i] = KEYS_IN_ORDER.get(i);
        }
        return errorArr;
    }

    public static String[] getActuators() {
        final String[] dropdownOptionsforActuators = new String[ACTUATOR_NAMES.size()];
        for (int i = 0; i < dropdownOptionsforActuators.length; i++) {
            dropdownOptionsforActuators[i] = ACTUATOR_NAMES.get(i);
        }
        return dropdownOptionsforActuators;
    }

    /**
     * Writes a HashMap of parsed errors to an output file in an elegant way. For
     * more information on the output file, check "README.txt" in the output folder.
     * 
     * @param values -> The HashMap object that will be printed to the file.
     * @throws IOException
     */
    private static void writeToFile(final HashMap<String, List<String>> values) throws IOException {
        final String fileName = LoggerFilter.fileName + " ROBOT_ERROR_IDENTIFIER";

        final String filePath = "output/mainoutput/" + fileName;
        final FileWriter fw = new FileWriter(filePath, false);
        final PrintWriter printer = new PrintWriter(fw);
        printer.println("Robot Malfunction(s):");
        for (final String s : KEYS_IN_ORDER) {
            printer.println("\"" + s + "\"\nStart: " + values.get(s).get(0) + "   End: " + values.get(s).get(1)
                    + "   Frequency: " + values.get(s).get(2) + "\n");
        }
        printer.close();
        LoggerGUI.printToFrame("Base output printed successfully to file at "
                + new File("output\\mainoutput\\" + fileName).getAbsolutePath());
    }

    /**
     * Allows you to view errors preceeding one of your choice. Amount of previous
     * errors to view can be selected. Cannot be compounded.
     * 
     * @param s_error   -> The error of interest.
     * @param s_prevNum -> The amount of errors to view before the error of
     *                  interest.
     */
    public static void prevErrors(final String s_error, final String s_prevNum) {
        if (allLogs.values.get(s_error) != null) {
            int prevNum;
            try {
                prevNum = Integer.parseInt(s_prevNum);
            } catch (final NumberFormatException e) {
                LoggerGUI.printToFrame("NaI inputted, defaulting to 5 previous errors");
                prevNum = 5;
            }
            LoggerGUI.printToFrame("Up to " + prevNum + " errors before/first occurrence of \"" + s_error + "\"");
            int counter = 0;
            for (int i = 0; i <= allLogs.messages.indexOf(s_error); i++) {
                if (allLogs.messages.indexOf(s_error) - i <= prevNum) {
                    counter++;
                    if (allLogs.messages.indexOf(s_error) - i != 0) {
                        LoggerGUI.printToFrame(
                                counter + ": " + allLogs.messages.get(i) + " @t = " + allLogs.timeStamps.get(i), true);
                    } else {
                        LoggerGUI.printToFrame("\nError of Interest: " + allLogs.messages.get(i) + " @t = "
                                + allLogs.timeStamps.get(i), true);
                    }
                }
            }
            LoggerGUI.outputAccordingly();
        } else {
            LoggerGUI.printToFrame("Error does not exist, check spelling.");
        }
    }

    /**
     * Allows you to view a .txt file with all errors logged sequentially. Cannot be
     * compounded.
     */
    public static void showSeq() {
        try {
            final String filePath = "output\\commandoutput\\" + fileName + " ALLEVENTS";
            final FileWriter fw = new FileWriter(filePath, false);
            final PrintWriter printer = new PrintWriter(fw);
            printer.println("All Errors:");
            for (int i = 0; i < allLogs.messages.size(); i++) {
                printer.println(allLogs.messages.get(i) + " @t = " + allLogs.timeStamps.get(i));
            }
            LoggerGUI.printToFrame(
                    "Successfully printed to file at " + "output\\commandoutput\\" + fileName + " ALLEVENTS.txt");
            printer.close();
        } catch (final Exception e) {
            LoggerGUI.printToFrame("Failed to print all errors to file.");
            e.printStackTrace();
        }
    }

    /**
     * Displays a list of errors based on a start bound double and an end bound
     * double. Can be compounded.
     * 
     * @param s_sb -> The start bound double.
     * @param s_eb -> The end bound double.
     */
    public static void logsInRange(final String s_sb, final String s_eb) {
        final LogList finalParsed = new LogList();

        if (!compounding) {
            LoggerGUI.printToFrame("Parsing from all logs");
            toParse = allLogs;
        }

        double sb;
        try {
            final String line = s_sb;
            sb = Double.parseDouble(line);
        } catch (final NumberFormatException e) {
            LoggerGUI.printToFrame("Not a valid double, defaulting to 0");
            sb = 0;
        }
        double eb;
        try {
            final String line = s_eb;
            eb = Double.parseDouble(line);
        } catch (final NumberFormatException e) {
            LoggerGUI.printToFrame("Not a valid double, defaulting to 1");
            eb = 1;
        }
        try {
            LoggerGUI.printToFrame("Logs between timestamps " + sb + " and " + eb);
            for (int i = 0; i < toParse.timeStamps.size(); i++) {
                if ((Double.parseDouble(toParse.timeStamps.get(i))) >= sb
                        && (Double.parseDouble(toParse.timeStamps.get(i)) <= eb)) {
                    LoggerGUI.printToFrame(toParse.messages.get(i) + " @t = " + toParse.timeStamps.get(i), true);
                    finalParsed.messages.add(toParse.messages.get(i));
                    finalParsed.timeStamps.add(toParse.timeStamps.get(i));
                }
            }
            LoggerGUI.outputAccordingly();
            toParse = finalParsed;
        } catch (final NumberFormatException e) {
            LoggerGUI.printToFrame("Error with number formatting.");
        }
    }

    /**
     * Displays all logs of a certain PrintStyle. Can be compounded.
     * 
     * @param s_type -> The PrintStyle to look for.
     */
    public static void logsByType(String s_type) {
        LogList finalParsed = new LogList();
        try {
            for (int i = 0; i < TYPE_KEYS.length; i++) {
                if (!compounding && s_type.equalsIgnoreCase(TYPE_KEYS[i])) {
                    LoggerGUI.printToFrame("Parsing from full type log");
                    if (typeLogs.get(i) != null)
                        finalParsed = typeLogs.get(i);
                    else {
                        LoggerGUI.printToFrame("Type log: " + s_type + " is null, defaulting to all logs");
                        finalParsed = allLogs;
                    }
                } else if (s_type.equalsIgnoreCase(TYPE_KEYS[i])) {
                    for (int j = 0; j < typeLogs.get(i).messages.size(); j++) {
                        for (int k = 0; k < toParse.messages.size(); k++) {
                            if (toParse.messages.get(k).equalsIgnoreCase(typeLogs.get(i).messages.get(j))
                                    && toParse.timeStamps.get(k).equalsIgnoreCase(typeLogs.get(i).timeStamps.get(j))) {
                                finalParsed.messages.add(toParse.messages.get(k));
                                finalParsed.timeStamps.add(toParse.timeStamps.get(k));
                                break;
                            }
                        }
                    }
                }
            }
            toParse = finalParsed;
        } catch (final NullPointerException e) {
            LoggerGUI.printToFrame("Invalid log type, defaulting to error");
            s_type = "Error";
            toParse = typeLogs.get(2);
        }
        LoggerGUI.printToFrame("All messages of type: " + s_type);
        for (int i = 0; i < toParse.messages.size(); i++) {
            LoggerGUI.printToFrame(toParse.messages.get(i) + " @t = " + toParse.timeStamps.get(i), true);
        }
        LoggerGUI.outputAccordingly();
    }

    /**
     * Displays all logs of a certain subsystem. Can be compounded.
     * 
     * @param s_type
     */
    public static void logsBySubsystem(String s_type) {
        LogList finalParsed = new LogList();

        try {
            for (int i = 0; i < SUBSYSTEM_KEYS.length; i++) {
                if (!compounding && s_type.equalsIgnoreCase(SUBSYSTEM_KEYS[i])) {
                    LoggerGUI.printToFrame("Parsing from full subsystem log");
                    if (subsystemLogs.get(i) != null)
                        finalParsed = subsystemLogs.get(i);
                    else {
                        LoggerGUI.printToFrame(s_type + " log is null, defaulting to all logs");
                        finalParsed = allLogs;
                    }
                } else if (s_type.equalsIgnoreCase(SUBSYSTEM_KEYS[i])) {
                    for (int j = 0; j < subsystemLogs.get(i).messages.size(); j++) {
                        for (int k = 0; k < toParse.messages.size(); k++) {
                            if (toParse.messages.get(k).equalsIgnoreCase(subsystemLogs.get(i).messages.get(j))
                                    && toParse.timeStamps.get(k)
                                            .equalsIgnoreCase(subsystemLogs.get(i).timeStamps.get(j))) {
                                finalParsed.messages.add(toParse.messages.get(k));
                                finalParsed.timeStamps.add(toParse.timeStamps.get(k));
                                break;
                            }
                        }
                    }
                }
            }
            toParse = finalParsed;
        } catch (final NullPointerException e) {
            LoggerGUI.printToFrame("Invalid subsystem type, defaulting to " + SUBSYSTEM_KEYS[0]);
            s_type = SUBSYSTEM_KEYS[0];
            toParse = subsystemLogs.get(0);
        }
        LoggerGUI.printToFrame("All messages of subsystem type: " + s_type);
        for (int i = 0; i < toParse.messages.size(); i++) {
            LoggerGUI.printToFrame(toParse.messages.get(i) + " @t = " + toParse.timeStamps.get(i), true);
        }
        LoggerGUI.outputAccordingly();
    }

    /**
     * Displays all logs that contain a certain word or phrase. Can be compounded.
     * 
     * @param s_key -> The word or phrase to look for.
     */
    public static void logsByKeyword(final String s_key) {
        final LogList finalParsed = new LogList();
        LogList toParseTemp = allLogs;
        if (compounding) {
            toParseTemp = toParse;
        }
        for (int i = 0; i < toParseTemp.messages.size(); i++) {
            if (toParseTemp.messages.get(i).toLowerCase().contains(s_key.toLowerCase())) {
                finalParsed.messages.add(toParseTemp.messages.get(i));
                finalParsed.timeStamps.add(toParseTemp.timeStamps.get(i));
            }
        }
        LoggerGUI.printToFrame("All messages containing keyword: " + s_key);
        for (int i = 0; i < finalParsed.messages.size(); i++) {
            LoggerGUI.printToFrame(finalParsed.messages.get(i) + " @t = " + finalParsed.timeStamps.get(i), true);
        }
        LoggerGUI.outputAccordingly();
        toParse = finalParsed;
    }

    public static void createGraph(final String type, String start, String end) {
        final String[] types = { "Line Graph (All Messages over Time)", "Bar Graph (Message Types by Count)",
                "Pie Chart (Subsystem Messages by Count)", "Multiline Graph (All Subsystem Messages over Time)" };

        double[] bounds = new double[2];
        try {
            bounds = new double[] { Double.valueOf(start), Double.valueOf(end) };
        } catch (final NumberFormatException e) {
            LoggerGUI.printToFrame("Invalid log range entered, defaulting to all logs.");
            bounds[0] = 0;
            bounds[1] = allLogs.timeStamps.size();
            start = "N/A";
            end = "N/A";
        }
        ArrayList<LogList> toGraphList = null;
        LoggerGUI.printToFrame("--------------------------------------------------");
        switch (type) {
        case "Pie Chart (Subsystem Messages by Count)":
            LoggerGUI.printToFrame(
                    "Creating pie chart with subsystem logs between timestamps: " + start + " and " + end);
            toGraphList = subsystemLogs;
            break;
        case "Bar Graph (Message Types by Count)":
            LoggerGUI.printToFrame("Creating bar graph with type logs between timestamps: " + start + " and " + end);
            toGraphList = typeLogs;
            break;
        case "Line Graph (All Messages over Time)":
            LoggerGUI.printToFrame("Creating line graph with all logs between timestamps: " + start + " and " + end);
            final ArrayList<LogList> tempLogs = new ArrayList<>();
            tempLogs.add(allLogs);
            toGraphList = tempLogs;
            break;
        case "Multiline Graph (All Subsystem Messages over Time)":
            LoggerGUI.printToFrame(
                    "Creating multiline graph(s) with subsystem logs between timestamps: " + start + " and " + end);
            toGraphList = subsystemLogs;
            break;
        }

        final LoggerGUI.GraphManager.GraphType[] gTypes = LoggerGUI.GraphManager.GraphType.values();
        for (int i = 0; i < types.length; i++) {
            if (types[i].equalsIgnoreCase(type)) {
                LoggerGUI.GraphManager.addGraph(gTypes[i], toGraphList, bounds);
                return;
            }
        }
    }

    /**
     * Opens the timemap
     */
    public static void openTimeMap() {

        LoggerGUI.OverviewManager.createSliderWindow(allLogs);
        LoggerGUI.OverviewManager.createOverview(allLogs);
    }

    /**
     * Sets compounding to a passed in boolean parameter.
     * 
     * @param c -> The boolean that determines the state of compounding.
     */
    public static void setCompounding(final boolean c) {
        compounding = c;
        LoggerGUI.printToFrame("Compounding set to " + c);
    }

    /**
     * Sets the variable "folderPath" to the String argument.
     * 
     * @param path -> The path to change folderPath to.
     */
    public static void setFilePath(final String path) {
        if (path.trim().equals("")) {
            getMostRecentFile();
        } else {
            wholePath = path;
        }
    }

    /**
     * Returns a String that represents the whole path of the current file being
     * parsed.
     * 
     * @return -> The full path of the file being parsed.
     */
    public static String getWholePath() {
        return wholePath;
    }

    /**
     * An Enum that contains command names, descriptions, number of parameters, and
     * parameter descriptions/types (in that order).
     */
    public enum Commands {

        preverr("Allows you to view errors preceding one of your choice.", 2,
                "[Error to parse for <Error Name>], [Numbers of previous errors to display <int>]"),
        showseq("Outputs a list of all errors in order into a .txt file.", 0, "[No parameters <N/A>]"),
        logsinrange("Allows you to view all errors within two timestamps. COMPOUNDABLE.", 2,
                "[Start timestamp <int>], [End timestamp <int>]"),
        logsbytype("Allows you to view errors of a certain PrintStyle. COMPOUNDABLE.", 1,
                "[PrintStyle to look for <Print Style>]"),
        logsbysubsystem("Allows you to view errors of a certain subsystem. COMPOUNDABLE.", 1,
                "[Subsystem to look for <Subsystem Name>]"),
        logsbykeyword("Allows you to view errors containing a specific word (not case sensitive). COMPOUNDABLE.", 1,
                "[Keyword to look for <String>]"),
        logsbyactuator("Allows you to view errors regarding specific actuators. COMPOUNDABLE.", 1,
                "[Actuator to look for <Actuator Name>]"),
        creategraph("Creates a graph from error types or subsystem data given a graph type.", 3,
                "[GraphType to look for <Graph Type>], [Start of range to parse through <int>], [End of range to parse through <int>]"),
        timemap("Unknown, will implement soon!", 0, "[No parameters <N/A>]");

        String desc;
        int paramNum;
        String paramDesc;

        private Commands(final String desc, final int params, final String paramDesc) {
            this.desc = desc;
            this.paramNum = params;
            this.paramDesc = paramDesc;
        }

        public int getParamNum() {
            return paramNum;
        }

        public String getDesc() {
            return desc;
        }

        public String getParamDesc() {
            return paramDesc;
        }
    }

    /**
     * A class that can store messages and timestamps of a certain filter type.
     */
    public static class LogList {
        public ArrayList<String> messages;
        public ArrayList<String> timeStamps;
        public HashMap<String, List<String>> values;

        public LogList() {
            messages = new ArrayList<>();
            timeStamps = new ArrayList<>();
            values = new HashMap<>();
        }

        public LogList(final ArrayList<String> messages, final ArrayList<String> timeStamps,
                final HashMap<String, List<String>> values) {
            this.messages = messages;
            this.timeStamps = timeStamps;
            this.values = values;
        }

        public String toString() {
            return messages.toString() + " " + timeStamps.toString();
        }
    }
}