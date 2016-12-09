#!/bin/bash

# Log file location
logfile=/home/pi/NEMETODE/logs/nemetode.log

# Override file location
overridefile=/home/pi/NEMETODE/logs/override.txt

# Video processing script location
process_videos=/home/pi/NEMETODE/scripts/process_videos.sh

# Shell variables to store pins connected to relay
pin1=10
pin2=24

# Latitude and longitude of observing site, in floating-point degrees
latitude=56.126957N
longitude=4.085583W

echo "" >> ${logfile}
echo "" >> ${logfile}

echo $(date) "> Scheduling system startup for 30 mins after sunset:" >> ${logfile}

echo $(date) "> " $(/usr/local/bin/sunwait -p 56.126957N 4.085583W | grep 'Sun rises')  >> ${logfile}

/usr/local/bin/sunwait sun down +00:30:00 ${latitude} ${longitude}

echo $(date) "> Reached 30 mins after sunset! Time to startup the system..." >> ${logfile}

echo $(date) "> Checking weather conditions!" >> ${logfile}

# Weather conditions check
# ------------------------
# ICAO weather station codes:
# EGPF - Glasgow Airport
# EGPH - Edinburgh Airport
# EGPN - Dundee Airport

# Get full weather forecast
forecast=$(/usr/bin/weather EGPF)
# Extract sky conditions string (note we need to put $forecast in quotes otherwise
# the new lines disappear and we can't grep out the sky conditions line)
sky=$(echo "${forecast}" | grep 'Sky conditions' | awk -F ':' '{print $2}')
# Trim white space from start & end of string so it's easier to compare
skyTrimmed="$(echo -e " ${sky} " | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
sky=$skyTrimmed

echo $(date) "> Glasgow sky conditions: " ${sky} >> ${logfile}

# If Glasgow weather forecast didn't provide sky conditions, check Edinburgh
if [ -z "$sky" ]; then
    forecast=$(/usr/bin/weather EGPH)
    sky=$(echo "${forecast}" | grep 'Sky conditions' | awk -F ':' '{print $2}')
    skyTrimmed="$(echo -e " ${sky} " | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
    sky=$skyTrimmed
    echo $(date) "> Edinburgh sky conditions: " ${sky} >> ${logfile}
fi

# If Edinburgh weather forecast didn't provide sky conditions, check Dundee
if [ -z "$sky" ]; then
    forecast=$(/usr/bin/weather EGPN)
    sky=$(echo "${forecast}" | grep 'Sky conditions' | awk -F ':' '{print $2}')
    skyTrimmed="$(echo -e " ${sky} " | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
    sky=$skyTrimmed
    echo $(date) "> Dundee sky conditions: " ${sky} >> ${logfile}
fi

echo $(date) "> Checking the manual override..." >> ${logfile}

override=$(cat ${overridefile})

echo $(date) "> Override is set to " ${override} >> ${logfile}

if [ "$override" == "NeverOn" ]; then
	echo $(date) "> Manual override NeverOn: Aborting startup!" >> ${logfile}
	exit
elif [ "$override" == "AlwaysOn" ]; then
	echo $(date) "> Manual override AlwaysOn: Skipping weather check!" >> ${logfile}
elif [ "$override" == "WeatherCheck" ]; then
	echo $(date) "> Manual override WeatherCheck: Checking sky conditions!" >> ${logfile}

	if [ "$sky" == "overcast" ] || [ "$sky" == "mostly cloudy" ]; then
    		echo $(date) "> Sky is " ${sky} " so we are NOT observing tonight." >> ${logfile}
    		exit
	elif [ "$sky" == "partly cloudy" ] || [ "$sky" == "mostly clear" ] || [ "$sky" == "clear" ]; then
    		echo $(date) "> Sky is " ${sky} " so we are OK to observe tonight." >> ${logfile}
	else
    		echo $(date) "> Can't interpret sky conditions: " ${sky} >> ${logfile}
    		echo $(date) "> Not observing tonight as a precaution. " >> ${logfile}
    		exit
	fi
else
	echo $(date) "> Can't interpret manual override: " ${override} >> ${logfile}
	echo $(date) "> Not observing tonight as a precaution. " >> ${logfile}
        exit
fi

echo $(date) "> Turning on the main computer..." >> ${logfile}

/usr/local/bin/gpio -g write ${pin1} 0
sleep 1
/usr/local/bin/gpio -g write ${pin1} 1

echo $(date) "> Turning on the camera..." >> ${logfile}

/usr/local/bin/gpio -g write ${pin2} 0

echo $(date) "> Scheduling system shutdown for 30 mins before sunrise:" >> ${logfile}

/usr/local/bin/sunwait sun up -00:30:00 ${latitude} ${longitude}

echo $(date) "> Turning off the camera..." >> ${logfile}

/usr/local/bin/gpio -g write ${pin2} 1


# UFOCapture is scheduled to halt detection at 30 mins before sunrise.
# We now wait til sunrise itself (in order to allow for different sunset
# times in UFOCapture/sunwait) before processing the videos from the previous
# night.

/usr/local/bin/sunwait sun up 00:00:00 ${latitude} ${longitude}

echo $(date) "> Processing last night's videos..." >> ${logfile}

# Invoke script to process last night's videos
${process_videos}

echo $(date) "> Observing complete!" >> ${logfile}
