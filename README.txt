SETUP:
Please set the "folderpath" config variable in the "config.txt" to the location where all of your .dslogs
and .dsevents are stored. This filepath can be located through the use of Driver Station Log File Viewer (Which can be opened through Driver Station).
Subsystem keywords should be typed out in the config file within the "{}". Example: {Drive, Hopper, Climb, Intake, Limelight}
Also, please add a "Console Overflow Limit" number (MUST BE AN INTEGER) into the "config.txt" file. Example: Console Overflow Limit: 100
If you don't know what you want, set it to a number greater than 100 or leave it blank.

BASIC USAGE:
Filtered files will appear in the "output" folder. They may be generated as a binary file with no extensions. If this is the case, simply
add ".txt" to the end of the filename manually or open the file with notepad.
They include: The name of the error, when it first appeared, when it last appeared, and how many times it appeared.
They will share the same name as the .dsevents file that was parsed, with "ROBOT_ERROR_IDENTIFIER" concatenated onto the end of the filename.
Further parsing can be done through the use of commands which you can access within the GUI.
To see what each command does, hover over the buttons in the command panel and a tooltip will show up describing the command.
Files outputted through the use of Commands can be found in "output\commandoutputs".

ADVANCED:
COMPOUNDING: A very useful tool. If a command can be compounded, then you can string it together with another compoundable command.
How this works:
Execute your initial command
Set COMPOUNDING to true by clicking the button on the control panel
Execute your second compoundable command and it will only return values that fall within the parameters you set for both commands.
Ex: Logs within t = 5.00 and t = 7.00 that are also Errors.
Because these results should be nice and small, they are printed to the viewing window.

HOTKEYS: CTRL + {Q, C, G, D, S}
Q: Quit
C: Open command panel
G: generate initial/main output
D: Open directory input to select file to parse
S: Save console into a text file
This java swing LookAndFeel uses TAB to navigate between JComponents and SPACE to perform a click action.
Use TAB to switch between buttons and any other non-textarea fields. press SPACE to click the component.

How to make our logger happy:
Messages: ### ###
Warnings: <<< >>>
Errors: !!! !!!
Sensor Readings: ||| |||

For the logger to be able to detect messages, all prints to console must be formatted in the way shown below:
Note: Timestamp should be two decimal places.

    Messages: ### ###
        Example: S_LOG ### <timestamp> "message" ### E_LOG
    Warnings: <<< >>>
        Example: S_LOG <<< Warning: <timestamp> "message" >>> E_LOG
    Errors: !!! !!!
        Example: S_LOG !!! Error: <timestamp> "message" !!! E_LOG
    Sensor Readings: ||| |||
        Example: S_LOG ||| Sensor Reading: <timestamp> "message" ||| E_LOG
        Note: format sensor reading to a reasonable number to not overflow console.

Note: These should all be printed to the system terminal (System.out.println(message);) and will automatically be sent to the .dsevents file.

If you want to tag errors by subsystem, please add the subsystem name (case sensitive) as you declared in the "config.txt" file.
Example: S_LOG !!! Error: <timestamp> "SUBSYSTEM_NAME_HERE: message" !!! E_LOG

Actuators are components (Talons, solenoids, etc.). If you want to view errors coming from a specific actuator, surround the name of the actuator
with "@".
Example: S_LOG !!! Error: <timestamp> "@ACTUATOR_NAME_HERE@ message" !!! E_LOG

These filter types can be combined.
Example: S_LOG !!! Error: <timestamp> "SUBSYSTEM_NAME_HERE: @ACTUATOR_NAME_HERE@ message" !!! E_LOG
Example (Filled): S_LOG !!! Error: <29.76> Drive: @Right Front Master@ Motor burnt out !!! E_LOG

These special characters should be tagged onto your messages in your "printing to .dsevents" class. (We call it TelemetryUtil).

Examples of what the client can parse through:

Input:
S_LOG ### <1.00> Robot starting to beep loudly ### E_LOG
S_LOG <<< Warning: <1.01> Timing Overrun >>> E_LOG
S_LOG !!! Error: <2.75> Encoder Disconnected !!! E_LOG
S_LOG ||| Sensor Reading: <2.99> Limit Switch Dead ||| E_LOG
S_LOG <<< Warning: <4.01> Timing Overrun >>> E_LOG
S_LOG !!! Error: <5.75> Encoder Disconnected !!! E_LOG
S_LOG ||| Sensor Reading: <5.99> Limit Switch Dead ||| E_LOG
S_LOG ### <6.00> Robot done beeping loudly ### E_LOG

Output:
Robot Malfunction(s):
"Robot done beeping loudly"
Start: 6.00   End: 6.00   Frequency: 1

"Encoder Disconnected"
Start: 2.75   End: 5.75   Frequency: 2

"Robot starting to beep loudly"
Start: 1.00   End: 1.00   Frequency: 1

"Limit Switch Dead"
Start: 2.99   End: 5.99   Frequency: 2

"Timing Overrun"
Start: 1.01   End: 4.01   Frequency: 2