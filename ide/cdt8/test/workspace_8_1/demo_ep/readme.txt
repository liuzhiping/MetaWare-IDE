This example allows the user to experiment with different power management settings.

We have modified 4 of the demo functions to allow task specific DVFS settings. We have also
added a delay function to simulate a load. Each of the 4 tasks can call this delay with a 
specific value. The remaining tasks in the application can also call the delay, but they
all use the same Global delay setting.

We have also added support for Global and Automatic DVFS.

In the demo.h file, we have added several definitions.

DEMO_AUTO_DVFS        - Set this to 1 to enable the Automatic DVFS mode
DEMO_POWER_DOWN       - This enables or disables Power Down modes. Set to 0 to turn off the Power Down feature.
                        Any other value is used to set the threshold for the Power Down feature.
DEMO_SLOW_DOWN        - This enables or disables Periodic Timer interval increases. Set to 0 to turn off this feature.
                        Any other value is used to indicate the number of times the timer interval can be increased.
DEMO_GLOBAL_DVFS_MODE - This sets the DVFS mode for all tasks that do not have a specific DVFS mode. Set to 0 to turn off.
SENDER_DVFS_MODE      - This sets the DVFS mode for the Sender task. Set to 0 for no Task specific DVFS setting.
RESPONDER_DVFS_MODE   - This sets the DVFS mode for the Responder task. Set to 0 for no Task specific DVFS setting.
EVENTA_DVFS_MODE      - This sets the DVFS mode for the Event A task. Set to 0 for no Task specific DVFS setting.
EVENTB_DVFS_MODE      - This sets the DVFS mode for the Event B task. Set to 0 for no Task specific DVFS setting.
GDVFS_DELAY           - This sets the delay/load for all tasks except those listed below.
SENDER_DELAY          - This sets the load/delay used in the Sender task.
RESPONDER_DELAY       - This sets the load/delay used in the Responder task.
EVENTA_DELAY          - This sets the load/delay used in the Event A task.
EVENTB_DELAY          - This sets the load/delay used in the Event B task.

All delays must be in the range of 0-100.

The default settings provide a fairly even distribution. The program is designed to be run on H/W or the ISS
using the MetaWare debugger with the MQX RTOS plug-in enabled. Log data is saved in a .dat file and feed to
Performance Tool for viewing and analysis. Power consumption data and frequencies can be adjusted in the file
link.met in the BSP directory.



