#!/bin/bash
#
# This script is designed to run once when the raspberry pi
# boots up. It configures the GPIO pins to be used to toggle
# the relay.

# Location of log file
logfile=/home/pi/NEMETODE/logs/nemetode.log

# Shell variables to store pins connected to relay
pin1=10
pin2=24

# Pause for 30s before doing anything - this seems to be necessary in order to
# write the log file (maybe the filesystem isn't fully booted at this point).
sleep 30

echo "" >> ${logfile}
echo "" >> ${logfile}
echo $(date) "> |============= Reboot =============|" >> ${logfile}

echo $(date) "> invoking pin_setup.sh" >> ${logfile}

# Configure GPIO pins as outputs. These will toggle the relays.
# They are mapped to the relay inputs as follows:
# pin1 -> IN1 (computer ON)
# pin2 -> IN2 (camera/iris ON/OFF)
/usr/local/bin/gpio -g mode ${pin1} out
/usr/local/bin/gpio -g write ${pin1} 1

/usr/local/bin/gpio -g mode ${pin2} out
/usr/local/bin/gpio -g write ${pin2} 1

echo $(date) "> pin setup complete!" >> ${logfile}
