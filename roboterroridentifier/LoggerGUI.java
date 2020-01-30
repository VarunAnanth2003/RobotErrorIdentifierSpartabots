package roboterroridentifier;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import java.awt.event.*;
import java.awt.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.image.BufferedImage;

import roboterroridentifier.LoggerFilter.LogList;

/**
 * GUI for LoggerFilter.java. Creates a java swing JFrame that contains
 * everything you will ever need for robot error parsing!
 */
public class LoggerGUI {
    /**
     * JFrames that will be displayed for the homescreen and for input.
     */
    public static JFrame f, inputf = new JFrame();
    /**
     * NamedJButtons to navigate around the GUI that will be on the homescreen.
     */
    public static NamedJButton qui, cmd, gen, dir, txt;
    /**
     * JTextArea for the main output console.
     */
    public static JTextArea ta;
    /**
     * JScrollPane for the main output console JTextArea.
     */
    public static JScrollPane scrollingta;
    /**
     * JLabel that is reused for titles.
     */
    public static JLabel titleText;
    /**
     * An array of commands generated from the "Commands" enum in LoggerFilter.
     */
    private static LoggerFilter.Commands[] arrayOfCmds;
    /**
     * An array of command buttons. Can be expanded to fit more buttons.
     */
    private static ArrayList<JButton> buttons = new ArrayList<>();
    /**
     * Color to be used in the GUI (R: 51, G: 90, B: 64).
     */
    public static Color spartaGreen = new Color(51, 90, 64);
    /**
     * Color to be used in the GUI (R: 255, G: 255, B: 255).
     */
    public static Color plainWhite = new Color(255, 255, 255);
    /**
     * Color to be used in the GUI (R: 162, G: 180, B: 168).
     */
    public static Color textAreaGreen = new Color(162, 180, 168);

    /**
     * Executes the GUI!
     */
    public static void executeGUI() {
        makeDirs();
        LoggerFilter.getConfig();
        setLookAndFeel();
        f = new JFrame();
        setupFrame();
        final Color c = Color.black;
        f.setBackground(c);
        f.setVisible(true);
        printToFrame("Robot Error Identifier (and other fun cheerios) made with love by Team 2976, The Spartabots!");
        printToFrame("Hotkeys: CTRL + {Q, C, G, O, S} for the buttons below.");
        if (LoggerFilter.fileName.equals("")) {
            LoggerFilter.getMostRecentFile();
        }
        setupListeners();
        printToFrame("File to scan: " + LoggerFilter.getWholePath());
    }

    /**
     * Makes output directories if they don't exist.
     */
    private static void makeDirs() {
        final File[] outputFolder = { new File("output"), new File("output\\commandoutput"),
                new File("output\\mainoutput"), new File("output\\savedfiles") };
        for (final File f : outputFolder) {
            if (!f.exists()) {
                f.mkdir();
            }
        }
    }

    /**
     * Sets the look and feel of the java swing panel. This changes all buttons and
     * UI elements and may mess up proportions. Edit with caution.
     */
    public static void setLookAndFeel() {
        try {
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method that takes in a text area and adds a keybound commands to it that
     * trigger buttons with CTRL + a key defined in the NamedJButton class.
     * 
     * @param ta         -> Text area to add actions bound to keymaps to.
     * @param allButtons -> ArrayList<NamedJButton> of all NamedJButtons that should
     *                   have hotkeys assigned to them.
     */
    private static void adaptiveListener(final JTextArea ta, final ArrayList<NamedJButton> allButtons) {
        for (int i = 0; i < allButtons.size(); i++) {
            final NamedJButton jb = allButtons.get(i);
            final Action a = new AbstractAction() {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    jb.doClick();
                }
            };
            ta.getInputMap().put(KeyStroke.getKeyStroke(allButtons.get(i).getHotkey()), a);
        }
    }

    /**
     * Sets up button listeners on the homescreen.
     */
    private static void setupListeners() {
        gen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                LoggerFilter.executeLogger();
                cmd.setEnabled(true);
                txt.setEnabled(true);
            }
        });

        cmd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                makeButtons();
            }
        });

        qui.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                f.dispose();
            }
        });

        txt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Calendar c = Calendar.getInstance();
                final String filePath = "output\\savedfiles\\";
                final String fileName = LoggerFilter.fileName + " SAVED_INFO_" + c.get(Calendar.HOUR_OF_DAY) + "_"
                        + c.get(Calendar.MINUTE) + "_" + c.get(Calendar.SECOND);
                FileWriter fw;
                try {
                    fw = new FileWriter(filePath + fileName, false);
                    final PrintWriter printer = new PrintWriter(fw);
                    printer.println("Saved info:");
                    printer.println(ta.getText());
                    printer.close();
                } catch (final IOException e1) {
                    printToFrame("Failed to save file.");
                    e1.printStackTrace();
                }
                printToFrame(
                        "Saved current console text to: " + new File(filePath + fileName).getAbsolutePath() + ".txt");
            }
        });

        dir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JFrame tempJ = new JFrame();
                tempJ.setSize(new Dimension(400, 500));
                tempJ.setLocationRelativeTo(null);
                tempJ.setTitle("Options Panel");
                tempJ.setLayout(new BorderLayout());
                tempJ.setResizable(false);
                final JButton chg = new JButton("CHANGE");
                chg.setBounds(125, 350, 150, 50);
                final JLabel jlb = new JLabel("File to parse (full filepath):", SwingConstants.CENTER);
                jlb.setBounds(0, 0, 400, 50);
                final JTextArea jtadir = new JTextArea(1, 5);
                final JScrollPane jspdir = new JScrollPane(jtadir, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                jspdir.setBounds(20, 40, 350, 35);
                final JLabel jlbsub = new JLabel("Subsystem List (comma separated):", SwingConstants.CENTER);
                jlbsub.setBounds(0, 100, 400, 50);
                final JTextArea jtasub = new JTextArea(1, 5);
                final JScrollPane jspsub = new JScrollPane(jtasub, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                jspsub.setBounds(20, 140, 350, 35);
                final JLabel jlbovr = new JLabel("Console Overflow Limit (int):", SwingConstants.CENTER);
                jlbovr.setBounds(0, 200, 400, 50);
                final JTextArea jtaovr = new JTextArea(1, 5);
                final JScrollPane jspovr = new JScrollPane(jtaovr, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                jspovr.setBounds(150, 240, 100, 35);
                chg.setBackground(spartaGreen);
                chg.setForeground(plainWhite);
                tempJ.add(chg);
                tempJ.add(jspdir);
                tempJ.add(jlb);
                tempJ.add(jspsub);
                tempJ.add(jlbsub);
                tempJ.add(jspovr);
                tempJ.add(jlbovr);
                final JPanel p = new JPanel();
                tempJ.add(p);
                tempJ.setVisible(true);
                if (chg.getActionListeners().length < 1) {
                    chg.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            LoggerFilter.setFilePath(jtadir.getText().trim().replaceAll("\\\\", "\\\\\\\\"));
                            if (jtadir.getText().trim().equals("")) {
                                printToFrame("Got the most recent file.");
                                printToFrame("Set file to parse to: " + LoggerFilter.getWholePath());
                            } else {
                                printToFrame("Set file to parse to: " + jtadir.getText().trim());
                                gen.setEnabled(true);
                            }
                            if (!jtasub.getText().trim().equals(""))
                                LoggerFilter.setSubsystemKeywords(jtasub.getText().trim());
                            if (!jtaovr.getText().trim().equals(""))
                                LoggerFilter.setOverflowLimit(jtaovr.getText().trim());
                        }
                    });
                }
            }
        });
    }

    public static ArrayList<String> messages = new ArrayList<>();
    public static int numOfLinesAllowed;

    /**
     * Strings passed into this command are printed into the GUI output screen.
     * Great for debugging and for user-viewable output.
     * 
     * @param s -> The string to print to the GUI screen.
     */
    public static void printToFrame(final String s) {
        ta.append(s + "\n");
    }

    /**
     * Strings passed into this command are sent into an ArrayList<String> of
     * messages. outputAccordingly() should be called outside of the iterative for
     * loop in which this overloaded version of printToFrame() is called.
     * 
     * @param s          -> The string to print to either the GUI screen or an
     *                   external file.
     * @param isAdaptive -> Should this be checked for line length?
     */
    public static void printToFrame(final String s, final boolean isAdaptive) {
        messages.add(s);
    }

    /**
     * Reads in all the messages in the ArrayList<String> messages and sees how many
     * lines there are in it. If there are more lines than Console Overflow Limit
     * (found in "config.txt") allows for, the output will go to an external file
     * with a timestamp.
     */
    public static void outputAccordingly() {
        printToFrame("");
        numOfLinesAllowed = LoggerFilter.overflowLineMax;
        final String filePath = "output\\commandoutput\\";
        final Calendar c = Calendar.getInstance();
        final String fileName = "LARGE_OUTPUT_" + c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE) + "_"
                + c.get(Calendar.SECOND);
        if (messages.size() != 0) {
            final int i = messages.size();

            if (i <= numOfLinesAllowed) {
                for (int j = 0; j < messages.size(); j++) {
                    ta.append(messages.get(j) + "\n");
                }
            } else {
                printToFrame(
                        "Output too large to display in console window. Outputted lines to " + filePath + fileName);
                FileWriter fw = null;
                try {
                    fw = new FileWriter(filePath + fileName, false);
                } catch (final IOException e) {
                    System.out.println("Failed to find large output file.");
                }
                final PrintWriter printer = new PrintWriter(fw);
                for (int j = 0; j < messages.size(); j++) {
                    printer.println(messages.get(j));
                }
                printer.close();
            }
            messages.clear();
        }
        printToFrame("");
        openOutput(filePath + fileName);
    }

    /**
     * Opens up large output files when made.
     * 
     * @param filePath -> Path to file to open.
     */
    public static void openOutput(final String filePath) {
        final File file = new File(filePath);
        if (!Desktop.isDesktopSupported()) {
            System.out.println("Desktop is not supported");
            return;
        }
        final Desktop desktop = Desktop.getDesktop();
        if (file.exists())
            try {
                desktop.open(file);
            } catch (final IOException e) {
                printToFrame("Could not open file.");
            }
    }

    /**
     * Sets up the main frame and all of its buttons.
     */
    private static void setupFrame() {
        final ArrayList<NamedJButton> mainButtons = new ArrayList<>();
        f.setSize(new Dimension(1280, 720));
        f.setLocationRelativeTo(null);
        f.setResizable(true);
        f.setTitle("Robot Error Identifier");
        f.setLayout(new BorderLayout());
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        titleText = new JLabel();
        titleText.setBounds(25, 10, 50, 50);
        titleText.setText("Output:");
        titleText.setFont(new Font(Font.DIALOG, Font.BOLD, titleText.getFont().getSize()));

        qui = new NamedJButton("Quit Button", "QUIT", "control Q");
        qui.setBounds(25, 600, 150, 50);
        qui.setToolTipText("Quits the program.");
        qui.setBackground(spartaGreen);
        qui.setForeground(plainWhite);
        qui.setFont(new Font(Font.DIALOG, Font.PLAIN, qui.getFont().getSize()));

        cmd = new NamedJButton("Command Button", "COMMANDS", "control C");
        cmd.setBounds(285, 600, 150, 50);
        cmd.setEnabled(false);
        cmd.setToolTipText("Opens a list of commands for filtering.");
        cmd.setBackground(spartaGreen);
        cmd.setForeground(plainWhite);
        cmd.setFont(new Font(Font.DIALOG, Font.PLAIN, qui.getFont().getSize()));

        gen = new NamedJButton("Generate Button", "GENERATE", "control G");
        gen.setBounds(565, 600, 150, 50);
        gen.setToolTipText("Parses file and generates basic output. Must be pressed first before COMMANDS or SAVE.");
        gen.setBackground(spartaGreen);
        gen.setForeground(plainWhite);
        gen.setFont(new Font(Font.DIALOG, Font.PLAIN, qui.getFont().getSize()));

        dir = new NamedJButton("Options Button", "OPTIONS", "control O");
        dir.setBounds(835, 600, 150, 50);
        dir.setToolTipText("Allows you to pick the file you want to parse.");
        dir.setBackground(spartaGreen);
        dir.setForeground(plainWhite);
        dir.setFont(new Font(Font.DIALOG, Font.PLAIN, qui.getFont().getSize()));

        txt = new NamedJButton("Save Button", "SAVE", "control S");
        txt.setBounds(1105, 600, 150, 50);
        txt.setEnabled(false);
        txt.setToolTipText("Saves current console view into a .txt file.");
        txt.setBackground(spartaGreen);
        txt.setForeground(plainWhite);
        txt.setFont(new Font(Font.DIALOG, Font.PLAIN, qui.getFont().getSize()));

        ta = new JTextArea(35, 100);
        scrollingta = new JScrollPane(ta);
        final JPanel p = new JPanel();
        ta.setBackground(textAreaGreen);

        mainButtons.add(qui);
        mainButtons.add(cmd);
        mainButtons.add(gen);
        mainButtons.add(dir);
        mainButtons.add(txt);

        adaptiveListener(ta, mainButtons);

        p.add(scrollingta);
        f.add(qui);
        f.add(cmd);
        f.add(gen);
        f.add(dir);
        f.add(txt);
        f.add(titleText);
        f.add(p);
        f.setVisible(true);
    }

    /**
     * Parses the description of the input parameters of a certain command (which
     * can be found as the third parameter in the "Commands" enum, and puts the
     * important elements from that String into an array. This array is later used
     * to create specific inputboxes for unique commands.
     * 
     * @param s -> The String parameter from the specific value from the "Commands"
     *          enum.
     */
    public static ArrayList<String> parseDesc(final String s) {
        final ArrayList<String> myList = new ArrayList<>();
        String description = s;
        while (description.contains("<") && description.contains(">")) {
            String inputType = description.substring(description.indexOf("<"), description.indexOf(">") + 1);
            description = description.replaceFirst(inputType, "");
            inputType = inputType.replaceAll("\\<", "");
            inputType = inputType.replaceAll("\\>", "");
            myList.add(inputType);
        }
        return myList;
    }

    /**
     * If the "COMMANDS" button is pressed, this method generates an
     * infinitely-expanding programmatically generated sequences of command buttons
     * that are based off of the "Commands" enum in LoggerFilter.
     */
    public static void makeButtons() {
        final JFrame tempJ = new JFrame();
        final int numOfCmnds = LoggerFilter.Commands.values().length;
        arrayOfCmds = LoggerFilter.Commands.values();
        for (int j = 0; j < numOfCmnds; j++) {
            final int finalJ = j;
            final String title = String.valueOf(arrayOfCmds[j]);
            buttons.add(new JButton(title));
            tempJ.setSize(new Dimension(950, 300 + (150 * (numOfCmnds / 5))));
            tempJ.setLocationRelativeTo(null);
            tempJ.setTitle("Command Panel");
            tempJ.setLayout(new BorderLayout());
            buttons.get(j).setBounds(40 + (j % 5 * 175), ((j / 5) * 75) + 75, 150, 50);
            buttons.get(j).setToolTipText(
                    arrayOfCmds[j].getDesc() + " Takes in " + arrayOfCmds[j].getParamNum() + " parameters.");
            buttons.get(j).setEnabled(true);
            buttons.get(j).setBackground(spartaGreen);
            buttons.get(j).setForeground(plainWhite);
            buttons.get(j).setFont(new Font(Font.DIALOG, Font.PLAIN, qui.getFont().getSize()));
            tempJ.add(buttons.get(j));
            titleText = new JLabel();
            titleText.setBounds(25, 10, 150, 50);
            titleText.setText("Command List:");
            tempJ.add(titleText);
            if (buttons.get(j).getActionListeners().length < 1) {
                buttons.get(j).addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        openInput(arrayOfCmds[finalJ], "ENTER");
                    }
                });
            }
        }
        final JButton homeButton = new JButton("HOME");
        homeButton.setBounds(40, 150 + (75 * (numOfCmnds / 5)), 150, 50);
        homeButton.setToolTipText("Takes you back to the home screen.");
        homeButton.setEnabled(true);
        homeButton.setBackground(spartaGreen);
        homeButton.setForeground(plainWhite);
        homeButton.setFont(new Font(Font.DIALOG, Font.PLAIN, qui.getFont().getSize()));
        final JButton compoundButton = new JButton("COMPOUNDING: OFF");
        compoundButton.setBounds(215, 150 + (75 * (numOfCmnds / 5)), 200, 50);
        compoundButton.setToolTipText("Enables and disables compounding.");
        compoundButton.setEnabled(true);
        compoundButton.setBackground(spartaGreen);
        compoundButton.setForeground(plainWhite);
        compoundButton.setFont(new Font(Font.DIALOG, Font.PLAIN, qui.getFont().getSize()));
        tempJ.add(homeButton);
        tempJ.add(compoundButton);
        final JPanel jp = new JPanel();
        tempJ.add(jp);
        tempJ.setVisible(true);
        if (homeButton.getActionListeners().length < 1) {
            homeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    tempJ.dispose();
                }
            });
        }
        if (compoundButton.getActionListeners().length < 1) {
            compoundButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    if (compoundButton.getText().equals("COMPOUNDING: OFF")) {
                        LoggerFilter.setCompounding(true);
                        compoundButton.setText("COMPOUNDING: ON");
                    } else if (compoundButton.getText().equals("COMPOUNDING: ON")) {
                        LoggerFilter.setCompounding(false);
                        compoundButton.setText("COMPOUNDING: OFF");
                    }
                }
            });
        }
    }

    /**
     * If a command button is pressed, this method is called. It will parse through
     * certain variables stored within Strings in the "Commands" enum and generates
     * input boxes accordingly. If the "submit" button is pressed, then the input is
     * passed into the "inputSwitch" method.
     * 
     * @param c             -> Element of the "Commands" enum that relates to the
     *                      button pressed.
     * @param hotkeyCounter -> The hotkey to map the submit button to.
     */
    public static void openInput(final LoggerFilter.Commands c, final String hotkeyCounter) {
        inputf = new JFrame();
        inputf.setSize(new Dimension(600, 400));
        inputf.setLocationRelativeTo(null);
        inputf.setResizable(false);
        inputf.setTitle("Input Panel");
        inputf.setLayout(new BorderLayout());
        final NamedJButton sub = new NamedJButton("Submit Button", "SUBMIT", hotkeyCounter);
        sub.setBounds(225, 300, 150, 50);
        sub.setBackground(spartaGreen);
        sub.setForeground(plainWhite);
        final ArrayList<String> parsedDesc = parseDesc(c.getParamDesc());
        final JPanel p = new JPanel(new FlowLayout());
        final ArrayList<JComboBox<Object>> allDrops = new ArrayList<>();
        final ArrayList<JTextArea> allInputField = new ArrayList<>();
        final Action submit = new AbstractAction("SUBMIT") {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(final ActionEvent e) {
                final ArrayList<String> input = new ArrayList<>();
                for (final JComboBox<Object> j : allDrops) {
                    input.add(getInput(j));
                }
                for (final JTextArea j : allInputField) {
                    input.add(getInput(j));
                }
                inputSwitch(input, c);
                inputf.dispose();
            }
        };
        int counter = 0;
        for (final String s : parsedDesc) {
            switch (s) {
            case "Error Name":
                final JComboBox<Object> jcbe = createDropdown(counter, LoggerFilter.getErrors());
                jcbe.setBackground(spartaGreen);
                jcbe.setForeground(plainWhite);
                allDrops.add(jcbe);
                inputf.add(jcbe);
                inputf.add(createLabel(counter, c.getParamDesc()));
                break;
            case "Print Style":
                final JComboBox<Object> jcbp = createDropdown(counter, LoggerFilter.TYPE_KEYS);
                jcbp.setBackground(spartaGreen);
                jcbp.setForeground(plainWhite);
                allDrops.add(jcbp);
                inputf.add(jcbp);
                inputf.add(createLabel(counter, c.getParamDesc()));
                break;
            case "Subsystem Name":
                final JComboBox<Object> jcbs = createDropdown(counter, LoggerFilter.SUBSYSTEM_KEYS);
                jcbs.setBackground(spartaGreen);
                jcbs.setForeground(plainWhite);
                allDrops.add(jcbs);
                inputf.add(jcbs);
                inputf.add(createLabel(counter, c.getParamDesc()));
                break;
            case "Actuator Name":
                final JComboBox<Object> jcbc = createDropdown(counter, LoggerFilter.getActuators());
                jcbc.setBackground(spartaGreen);
                jcbc.setForeground(plainWhite);
                allDrops.add(jcbc);
                inputf.add(jcbc);
                inputf.add(createLabel(counter, c.getParamDesc()));
                break;
            case "String":
                final JTextArea jtas = createtField(counter);
                final JScrollPane jsps = new JScrollPane(jtas, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                jtas.setBackground(textAreaGreen);
                jsps.setBounds(225, 50 + (counter * 70), 150, 20);
                jtas.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), submit);
                allInputField.add(jtas);
                inputf.add(jsps);
                inputf.add(createLabel(counter, c.getParamDesc()));
                break;
            case "int":
                final JTextArea jtai = createtField(counter);
                final JScrollPane jspi = new JScrollPane(jtai, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                jtai.setBackground(textAreaGreen);
                jspi.setBounds(275, 50 + (counter * 70), 50, 20);
                jtai.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), submit);
                allInputField.add(jtai);
                inputf.add(jspi);
                inputf.add(createLabel(counter, c.getParamDesc()));
                break;
            case "Graph Type":
                final String[] types = { "Line Graph (All Messages over Time)",
                        "Multiline Graph (All Subsystem Messages over Time)", "Bar Graph (Message Types by Count)",
                        "Pie Chart (Subsystem Messages by Count)" };
                final JComboBox<Object> jcbg = createDropdown(counter, types);
                jcbg.setBackground(spartaGreen);
                jcbg.setForeground(plainWhite);
                allDrops.add(jcbg);
                inputf.add(jcbg);
                inputf.add(createLabel(counter, c.getParamDesc()));
                break;
            case "N/A":
                inputf.add(createLabel(counter, c.getParamDesc()));
                break;
            default:
                printToFrame("Error with input panel generation");
            }
            counter++;
        }
        inputf.add(sub);
        inputf.add(p);
        inputf.setVisible(true);
        sub.addActionListener(new ActionListener() {
            ArrayList<String> input = new ArrayList<>();

            @Override
            public void actionPerformed(final ActionEvent e) {
                for (final JComboBox<Object> j : allDrops) {
                    input.add(getInput(j));
                }
                for (final JTextArea j : allInputField) {
                    input.add(getInput(j));
                }
                inputSwitch(input, c);
                inputf.dispose();
            }
        });

    }

    /**
     * Creates a dropdown meant for the programmatic input panel and returns it.
     * 
     * @param orderNum -> Where (vertically) this inputbox should go.
     * @param options  -> The options for the dropdown.
     * @return The dropdown (JComboBox<Object>) needed for the inputpanel.
     */
    public static JComboBox<Object> createDropdown(final int orderNum, final String[] options) {
        final JComboBox<Object> jcb = new JComboBox<>(options);
        jcb.setBounds(100, 50 + (orderNum * 70), 400, 20);
        return jcb;
    }

    /**
     * Creates a text area meant for the programmatic input panel and returns it.
     * 
     * @param orderNum -> Where (vertically) this inputbox should go.
     * @return The text area (JTextArea) needed for the inputpanel.
     */
    public static JTextArea createtField(final int orderNum) {
        final JTextArea jta = new JTextArea();
        jta.setSize(5, 5);
        return jta;
    }

    /**
     * Creates a descriptor JLabel for the programmatic inputboxes and returns it.
     * 
     * @param orderNum -> Where (vertically) this label should go.
     * @param desc     -> The description, gotten from a String parameter within the
     *                 "Commands" enum that should be applied to the JLabel.
     * @return The JLabel appropriate for a certain inputbox.
     */
    public static JLabel createLabel(final int orderNum, final String desc) {
        final String[] labelToAdd = desc.split("\\,");
        final JLabel addLabel = new JLabel(labelToAdd[orderNum], SwingConstants.CENTER);
        addLabel.setBounds(0, 30 + (orderNum * 70), 600, 20);
        return addLabel;
    }

    /**
     * Gets the input from a JComboBox<Object> (dropdown menu) and returns it as a
     * String.
     * 
     * @param dropDown -> The JComboBox<Object> to read.
     * @return The content from the JComboBox<Object>.
     */
    public static String getInput(final JComboBox<Object> dropDown) {
        final String input = dropDown.getSelectedItem().toString();
        input.trim();
        return input;
    }

    /**
     * Gets the input from a JTextArea and returns it as a String.
     * 
     * @param textArea -> The textarea to read.
     * @return The content from the JTextArea.
     */
    public static String getInput(final JTextArea textArea) {
        if (!textArea.getText().equals("")) {
            final Scanner myScanner = new Scanner(textArea.getText());
            final String reply = myScanner.nextLine();
            reply.trim();
            myScanner.close();
            return reply;
        } else {
            return "";
        }
    }

    /**
     * Input from programmatically generated input boxes as well as the type of
     * command is passed into here, and commands are sent out to LoggerFilter
     * accordingly.
     * 
     * @param input -> The inputarray to parse through.
     * @param c     -> The type of command.
     */
    public static void inputSwitch(final ArrayList<String> input, final LoggerFilter.Commands c) {
        switch (c) {
        case preverr:
            LoggerFilter.prevErrors(input.get(0), input.get(1));
            break;
        case showseq:
            LoggerFilter.showSeq();
            break;
        case logsinrange:
            LoggerFilter.logsInRange(input.get(0), input.get(1));
            break;
        case logsbytype:
            LoggerFilter.logsByType(input.get(0));
            break;
        case logsbysubsystem:
            LoggerFilter.logsBySubsystem(input.get(0));
            break;
        case logsbykeyword:
            LoggerFilter.logsByKeyword(input.get(0));
            break;
        case logsbyactuator:
            LoggerFilter.logsByKeyword(input.get(0));
            break;
        case creategraph:
            LoggerFilter.createGraph(input.get(0), input.get(1), input.get(2));
            break;
        case timemap:
            LoggerFilter.openTimeMap();
        }
        printToFrame("Command Complete.");
        printToFrame("--------------------------------------------------");
    }

    /**
     * An inner class that allows for the generation of unique JButtons with an id
     * parameter.
     */
    public static class NamedJButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final String id;
        private final String hotkey;

        public NamedJButton(final String id, final String name, final String hotkey) {
            super(name);
            this.id = id;
            this.hotkey = hotkey;
        }

        public String getId() {
            return id;
        }

        public String getHotkey() {
            return hotkey;
        }
    }

    /**
     * An inner class that allows for graph generation and display.
     */
    public static class GraphManager {
        public enum GraphType {
            LINE(new Line()), BAR(new Bar()), PIE(new Pie()), MULTILINE(new MultiLine());

            public GraphDraw getGraph() {
                return graph;
            }

            private final GraphDraw graph;

            GraphType(final GraphDraw graph) {
                this.graph = graph;
            }
        }

        public interface GraphDraw {
            void draw(ArrayList<LogList> data, double[] bounds);
        }

        public static void addGraph(final GraphType type, final ArrayList<LogList> dataSet, final double[] bounds) {
            type.getGraph().draw(dataSet, bounds);
        }

        public static int maxSec(final LogList lldata) {
            return lldata.timeStamps.size() < 1 ? 0
                    : (int) (Double.parseDouble(lldata.timeStamps.get(lldata.timeStamps.size() - 1)));
        }

        public static class Bar implements GraphDraw {
            @Override
            public void draw(final ArrayList<LogList> data, final double[] bounds) {
                final DefaultCategoryDataset objDataset = new DefaultCategoryDataset();
                final String[] labels = LoggerFilter.TYPE_KEYS;
                final int[] sizesInRange = new int[LoggerFilter.TYPE_KEYS.length];

                for (int i = 0; i < labels.length; i++) {
                    for (int j = 0; j < data.get(i).timeStamps.size(); j++) {
                        if (Double.valueOf(data.get(i).timeStamps.get(j)) > bounds[0]
                                && Double.valueOf(data.get(i).timeStamps.get(j)) < bounds[1]) {
                            sizesInRange[i]++;
                        }
                    }

                    objDataset.setValue(sizesInRange[i], labels[i], labels[i]);
                }

                final JFreeChart objChart = ChartFactory.createBarChart("Type Message Bar Graph", // Chart title
                        "Time", // Domain axis label
                        "Number of Messages", // Range axis label
                        objDataset, // Chart Data
                        PlotOrientation.VERTICAL, // orientation
                        true, // include legend?
                        true, // include tooltips?
                        false // include URLs?
                );

                final ChartFrame frame = new ChartFrame("SubsystemBar", objChart);
                frame.pack();
                frame.setVisible(true);
            }
        }

        public static class Line implements GraphDraw {
            @Override
            public void draw(final ArrayList<LogList> data, final double[] bounds) {
                final XYSeries objDataset = new XYSeries("All Messages Amount");

                final ArrayList<LogList> dataInRange = new ArrayList<>();
                dataInRange.add(new LogList());

                for (int j = 0; j < data.get(0).timeStamps.size(); j++) {
                    if (Double.valueOf(data.get(0).timeStamps.get(j)) > bounds[0]
                            && Double.valueOf(data.get(0).timeStamps.get(j)) < bounds[1]) {
                        dataInRange.get(0).timeStamps.add(data.get(0).timeStamps.get(j));
                        dataInRange.get(0).messages.add(data.get(0).messages.get(j));
                    }
                }
                final int maxSec = maxSec(dataInRange.get(0));
                final int[] summedData = new int[maxSec + 1];
                for (int i = 0; i < summedData.length; i++) {
                    final int bottomBound = i;
                    final int topBound = i + 1;
                    for (int j = 0; j < dataInRange.get(0).timeStamps.size(); j++) {
                        final double ts = Double.parseDouble(dataInRange.get(0).timeStamps.get(j));
                        if (ts > bottomBound && ts <= topBound) {
                            summedData[i]++;
                        }
                    }
                }
                for (int i = 0; i < summedData.length; i++) {
                    objDataset.add(i, summedData[i]);
                }
                final XYSeriesCollection xydata = new XYSeriesCollection(objDataset);
                final JFreeChart objChart = ChartFactory.createXYStepChart("All Messages Line Graph", // Chart title
                        "Time", // Domain axis label
                        "Number of Messages", // Range axis label
                        xydata, // Chart Data
                        PlotOrientation.VERTICAL, // orientation
                        true, // include legend?
                        true, // include tooltips?
                        false // include URLs?
                );
                final NumberAxis xAxis = new NumberAxis();
                xAxis.setTickUnit(new NumberTickUnit(objDataset.getMaxX() > 100 ? 5 : 1));
                final NumberAxis yAxis = new NumberAxis();
                yAxis.setTickUnit(new NumberTickUnit(1));
                final XYPlot plot = (XYPlot) objChart.getPlot();
                plot.setDomainAxis(xAxis);
                plot.setRangeAxis(yAxis);
                final ChartPanel cPanel = new ChartPanel(objChart);

                cPanel.setMouseZoomable(true);

                final JFrame frame = new JFrame("All Messages Line Graph");
                final JScrollPane chartScroll = new JScrollPane(cPanel);
                frame.getContentPane().add(chartScroll);
                frame.pack();
                frame.setVisible(true);
            }
        }

        public static class MultiLine implements GraphDraw {
            @Override
            public void draw(final ArrayList<LogList> data, final double[] bounds) {
                final ArrayList<LogList> dataInRange = new ArrayList<>();
                for (int i = 0; i < data.size(); i++) {
                    dataInRange.add(new LogList());
                }
                for (int i = 0; i < data.size(); i++) {
                    final XYSeries objDataset = new XYSeries(
                            "Messages in " + LoggerFilter.SUBSYSTEM_KEYS[i] + " by Amount");
                    for (int j = 0; j < data.get(i).timeStamps.size(); j++) {
                        if (Double.valueOf(data.get(i).timeStamps.get(j)) > bounds[0]
                                && Double.valueOf(data.get(i).timeStamps.get(j)) < bounds[1]) {
                            dataInRange.get(i).timeStamps.add(data.get(i).timeStamps.get(j));
                            dataInRange.get(i).messages.add(data.get(i).messages.get(j));
                        }
                    }
                    if (dataInRange.get(i).timeStamps.size() != 0) {
                        final int maxSec = maxSec(dataInRange.get(i));
                        final int[] summedData = new int[maxSec + 1];
                        for (int j = 0; j < summedData.length; j++) {
                            final int bottomBound = j;
                            final int topBound = j + 1;
                            for (int k = 0; k < dataInRange.get(i).timeStamps.size(); k++) {
                                final double ts = Double.parseDouble(dataInRange.get(i).timeStamps.get(k));
                                if (ts > bottomBound && ts <= topBound) {
                                    summedData[j]++;
                                }
                            }
                        }
                        for (int j = 0; j < summedData.length; j++) {
                            objDataset.add(j, summedData[j]);
                        }
                        final XYSeriesCollection xydata = new XYSeriesCollection(objDataset);
                        final JFreeChart objChart = ChartFactory.createXYStepChart(
                                "Messages in " + LoggerFilter.SUBSYSTEM_KEYS[i] + " by Amount", // Chart title
                                "Time", // Domain axis label
                                "Number of Messages", // Range axis label
                                xydata, // Chart Data
                                PlotOrientation.VERTICAL, // orientation
                                true, // include legend?
                                true, // include tooltips?
                                false // include URLs?
                        );
                        final NumberAxis xAxis = new NumberAxis();
                        xAxis.setTickUnit(new NumberTickUnit(objDataset.getMaxX() > 100 ? 5 : 1));
                        final NumberAxis yAxis = new NumberAxis();
                        yAxis.setTickUnit(new NumberTickUnit(1));
                        final XYPlot plot = (XYPlot) objChart.getPlot();
                        plot.setDomainAxis(xAxis);
                        plot.setRangeAxis(yAxis);
                        final ChartPanel cPanel = new ChartPanel(objChart);

                        cPanel.setMouseZoomable(true);

                        final JFrame frame = new JFrame("Subsystem Messages Multiline Graph");
                        final JScrollPane chartScroll = new JScrollPane(cPanel);
                        frame.getContentPane().add(chartScroll);
                        frame.pack();
                        frame.setVisible(true);
                    }
                }

            }
        }

        public static class Pie implements GraphDraw {
            @Override
            public void draw(final ArrayList<LogList> data, final double[] bounds) {
                final DefaultPieDataset objDataset = new DefaultPieDataset();
                final String[] labels = LoggerFilter.SUBSYSTEM_KEYS;

                final int[] sizesInRange = new int[labels.length];

                for (int i = 0; i < labels.length; i++) {
                    for (int j = 0; j < data.get(i).timeStamps.size(); j++) {
                        if (Double.valueOf(data.get(i).timeStamps.get(j)) > bounds[0]
                                && Double.valueOf(data.get(i).timeStamps.get(j)) < bounds[1]) {
                            sizesInRange[i]++;
                        }
                    }
                }
                for (int i = 0; i < data.size(); i++) {
                    objDataset.setValue(labels[i], sizesInRange[i]);
                }

                final JFreeChart pieChart = ChartFactory.createPieChart("Subsystem Type Messages by Count", // Chart
                                                                                                            // title
                        objDataset, // Chart Data
                        true, // include legend?
                        true, // include tooltips?
                        false // include URLs?
                );

                final ChartFrame frame = new ChartFrame("TypePie", pieChart);
                frame.pack();
                frame.setVisible(true);
                LoggerGUI.printToFrame("Pie graph of message types constructed successfully");
            }
        }
    }

    /**
     * A class to manage overviews.
     */
    public static class OverviewManager {
        private static JFrame sliderFrame;
        private static JPanel jp;
        private static JSlider sliderBar;
        private static JLabel jlb;
        private static JTextArea jta;
        private static JComboBox<Object> jcb;
        private static int tValue;
        private static LogList mData;
        private static JFrame imageFrame = new JFrame();;
        private static ImagePanel[] allImagePanels = new ImagePanel[0];
        private static String[] panelNames;
        private static JComboBox<Object> viewChooser;
        private static ArrayList<String> activeActuators = new ArrayList<>();
        private static int xPos;
        private static int yPos;
        private static JLabel pointLabel = new JLabel("Last clicked point: X, Y");

        public static void createSliderWindow(final LogList data) {
            mData = data;
            allImagePanels = Arrays.copyOf(allImagePanels, OverviewManager.ImageStorage.values().length);
            for (int i = 0; i < OverviewManager.ImageStorage.values().length; i++) {
                allImagePanels[i] = new ImagePanel(OverviewManager.ImageStorage.values()[i].getName(),
                        OverviewManager.ImageStorage.values()[i].getPath());
            }
            sliderFrame = new JFrame("Slider Frame");
            jp = new JPanel();
            jp.setLayout(new FlowLayout());
            sliderBar = new JSlider(0, LoggerGUI.GraphManager.maxSec(data), 0);
            jlb = new JLabel();
            final SliderListener s = new SliderListener();
            sliderBar.addChangeListener(s);
            String[] SUBSYSTEM_KEYS_EXTENDED = new String[LoggerFilter.SUBSYSTEM_KEYS.length + 1];
            SUBSYSTEM_KEYS_EXTENDED[0] = "All";
            for (int i = 1; i < SUBSYSTEM_KEYS_EXTENDED.length; i++) {
                SUBSYSTEM_KEYS_EXTENDED[i] = LoggerFilter.SUBSYSTEM_KEYS[i - 1];
            }
            jcb = new JComboBox<>(SUBSYSTEM_KEYS_EXTENDED);

            sliderBar.setBounds(50, 25, 200, 50);
            sliderBar.setPaintTrack(true);
            sliderBar.setPaintTicks(true);
            sliderBar.setPaintLabels(true);
            sliderBar.setMajorTickSpacing(25);
            sliderBar.setMinorTickSpacing(5);
            sliderBar.setBackground(spartaGreen);
            sliderBar.setForeground(plainWhite);

            jlb.setBounds(275, 25, 100, 50);
            jlb.setText("@t = " + sliderBar.getValue());

            jcb.setBounds(50, 125, 300, 20);
            jcb.setBackground(spartaGreen);
            jcb.setForeground(plainWhite);

            jta = new JTextArea();
            JScrollPane tlviewer = new JScrollPane(jta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            tlviewer.setBounds(0, 150, 400, 400);

            jcb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    jlb.setText("@t = " + sliderBar.getValue() + " - " + (sliderBar.getValue() + 1));
                    updateErrors(sliderBar.getValue());
                    updateGraphics();
                }
            });
            pointLabel.setBounds(125, 75, 400, 50);
            sliderFrame.add(sliderBar);
            sliderFrame.add(jlb);
            sliderFrame.add(jcb);
            sliderFrame.add(tlviewer);
            sliderFrame.add(pointLabel);
            sliderFrame.add(jp);
            sliderFrame.setSize(400, 600);
            sliderFrame.setResizable(false);
            sliderFrame.setVisible(true);
        }

        public static int getTValue() {
            return tValue;
        }

        public static void updateErrors(final int t) {
            activeActuators.clear();
            final LogList timedLog = new LogList();
            final int bottomBound = t;
            final int topBound = t + 1;
            for (int j = 0; j < mData.timeStamps.size(); j++) {
                final double ts = Double.parseDouble(mData.timeStamps.get(j));
                if (ts > bottomBound && ts <= topBound) {
                    timedLog.messages.add(mData.messages.get(j));
                    timedLog.timeStamps.add(mData.timeStamps.get(j));
                }
            }
            final ArrayList<LogList> subLogs = new ArrayList<>();
            for (int i = 0; i < LoggerFilter.SUBSYSTEM_KEYS.length; i++) {
                subLogs.add(new LogList());
            }
            for (int i = 0; i < timedLog.messages.size(); i++) {
                for (int j = 0; j < LoggerFilter.SUBSYSTEM_KEYS.length; j++) {
                    if (timedLog.messages.get(i).contains(LoggerFilter.SUBSYSTEM_KEYS[j])) {
                        subLogs.get(j).messages.add(timedLog.messages.get(i));
                        subLogs.get(j).timeStamps.add(timedLog.timeStamps.get(i));
                    }
                }
            }
            jta.setText("");
            for (int i = 0; i < LoggerFilter.SUBSYSTEM_KEYS.length; i++) {
                if (checkAllowedDisplay(i)) {
                    jta.append("Logs in " + LoggerFilter.SUBSYSTEM_KEYS[i] + ":\n");
                    for (int j = 0; j < subLogs.get(i).messages.size(); j++) {
                        jta.append(subLogs.get(i).messages.get(j) + " @t = " + subLogs.get(i).timeStamps.get(j) + "\n");
                        for (int k = 0; k < LoggerFilter.ACTUATOR_NAMES.size(); k++) {
                            if (subLogs.get(i).messages.get(j).contains(LoggerFilter.ACTUATOR_NAMES.get(k))) {
                                if (!activeActuators.contains(LoggerFilter.ACTUATOR_NAMES.get(k))) {
                                    activeActuators.add(LoggerFilter.ACTUATOR_NAMES.get(k));
                                }
                            }
                        }
                    }
                    jta.append("\n");
                }
            }
        }

        public static boolean checkAllowedDisplay(int n) {
            boolean canDo = false;
            if (jcb.getSelectedItem().toString().equals("All")) {
                canDo = true;
            } else {
                if (jcb.getSelectedItem().toString().equals(LoggerFilter.SUBSYSTEM_KEYS[n])) {
                    canDo = true;
                } else {
                    canDo = false;
                }
            }
            return canDo;
        }

        public static void createOverview(final LogList data) {
            panelNames = new String[allImagePanels.length];
            for (int i = 0; i < panelNames.length; i++) {
                panelNames[i] = allImagePanels[i].getName();
            }
            viewChooser = new JComboBox<Object>(panelNames);
            viewChooser.setBounds(100, 0, 400, 20);
            viewChooser.setBackground(spartaGreen);
            viewChooser.setForeground(plainWhite);
            imageFrame.add(viewChooser);
            viewChooser.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateGraphics();
                }
            });
            updateGraphics();
        }

        public static void updateGraphics() {
            for (ImagePanel ip : allImagePanels) {
                imageFrame.remove(ip);
            }
            for (int i = 0; i < allImagePanels.length; i++) {
                if (viewChooser.getSelectedItem().toString().equals(allImagePanels[i].getName())) {
                    imageFrame.add(allImagePanels[i]);
                    imageFrame.setName(allImagePanels[i].getName());
                    if (allImagePanels[i].getMouseListeners().length < 1) {
                        allImagePanels[i].addMouseListener(new ImageClickListener());
                    }

                }
            }
            imageFrame.repaint();
            imageFrame.pack();
            imageFrame.setVisible(true);
            imageFrame.setLocationRelativeTo(null);
            sliderFrame.requestFocus();
        }

        public static class SliderListener implements ChangeListener {
            @Override
            public void stateChanged(final ChangeEvent e) {
                jlb.setText("@t = " + sliderBar.getValue() + " - " + (sliderBar.getValue() + 1));
                updateErrors(sliderBar.getValue());
                updateGraphics();
                sliderBar.requestFocusInWindow();
            }
        }

        public static class ImageClickListener implements MouseListener {

            @Override
            public void mouseClicked(MouseEvent p) {
                pointLabel.setText("Last clicked point: " + p.getX() + ", " + p.getY());
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {

            }

            @Override
            public void mouseExited(MouseEvent arg0) {

            }

            @Override
            public void mousePressed(MouseEvent arg0) {

            }

            @Override
            public void mouseReleased(MouseEvent arg0) {

            }
        }

        public static class ImagePanel extends JPanel {
            private static final long serialVersionUID = 1L;
            private BufferedImage mImage;
            private String mName;
            private HashMap<String, List<Integer>> aCoords = new HashMap<String, List<Integer>>();

            public ImagePanel(String name, String filePath) {
                super.setName(name);
                mName = name;
                try {
                    mImage = ImageIO.read(new File(filePath));
                } catch (IOException e) {
                    printToFrame("Could not find image.");
                }
                manualDimension();
            }

            /**
             * Case statement strings should reflect names of ImagePanels declared at the
             * top of the class.
             */
            public void manualDimension() {
                switch (mName) {
                case "Top View":
                    aCoords.put("Left Master", Arrays.asList(179, 294));
                    aCoords.put("Left Slave", Arrays.asList(179, 352));
                    aCoords.put("Right Master", Arrays.asList(459, 295));
                    aCoords.put("Right Slave", Arrays.asList(456, 358));
                    aCoords.put("Limelight", Arrays.asList(317, 96));
                    aCoords.put("Intake Master", Arrays.asList(126, 648));
                    aCoords.put("Intake Slave", Arrays.asList(515, 648));
                    aCoords.put("Shooter Master", Arrays.asList(227, 100));
                    aCoords.put("Shooter Slave", Arrays.asList(410, 100));
                    break;
                case "Angle View":
                    aCoords.put("Left Master", Arrays.asList(189, 444));
                    aCoords.put("Left Slave", Arrays.asList(189, 487));
                    aCoords.put("Right Master", Arrays.asList(452, 445));
                    aCoords.put("Right Slave", Arrays.asList(452, 485));
                    aCoords.put("Intake Master", Arrays.asList(145, 588));
                    aCoords.put("Intake Slave", Arrays.asList(503, 588));
                    aCoords.put("Shooter Master", Arrays.asList(228, 135));
                    aCoords.put("Shooter Slave", Arrays.asList(403, 135));
                    aCoords.put("Top Piston", Arrays.asList(115, 281));
                    aCoords.put("Side Left Piston", Arrays.asList(90, 461));
                    aCoords.put("Side Right Piston", Arrays.asList(555, 461));
                    break;
                case "Isometric View":
                    aCoords.put("Left Master", Arrays.asList(182, 344));
                    aCoords.put("Left Slave", Arrays.asList(186, 386));
                    aCoords.put("Right Master", Arrays.asList(336, 416));
                    aCoords.put("Right Slave", Arrays.asList(338, 456));
                    aCoords.put("Intake Master", Arrays.asList(294, 542));
                    aCoords.put("Intake Slave", Arrays.asList(503, 636));
                    aCoords.put("Shooter Master", Arrays.asList(355, 117));
                    aCoords.put("Limelight", Arrays.asList(428, 114));
                    break;
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(mImage.getWidth(), mImage.getHeight() + 100);
            }

            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.drawImage(mImage, 0, 20, this);
                g2d.setStroke(new BasicStroke(10));
                g2d.setColor(Color.RED);
                for (int j = 0; j < activeActuators.size(); j++) {
                    if (aCoords.containsKey(activeActuators.get(j))) {
                        xPos = aCoords.get(activeActuators.get(j)).get(0);
                        yPos = aCoords.get(activeActuators.get(j)).get(1);
                        g2d.drawOval(xPos - 50, yPos - 50, 100, 100);
                    }
                }
                g2d.dispose();
            }

            public String getName() {
                return mName;
            }

            public HashMap<String, List<Integer>> getCoords() {
                return aCoords;
            }
        }

        public enum ImageStorage {
            TOP("Top View", "images\\F_Top_View_SB.png"), ANGLE("Angle View", "images\\F_Angle_View_SB.png"),
            ISO("Isometric View", "images\\F_Iso_View_SB.png");

            public String mName;
            public String mPath;

            private ImageStorage(String name, String path) {
                mName = name;
                mPath = path;
            }

            public String getName() {
                return mName;
            }

            public String getPath() {
                return mPath;
            }
        }
    }
}