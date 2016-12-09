#!/bin/bash
#
# Script used to monitor for loss of network events, which seems to happen
# from time to time with the pi. There is commented-out legacy code at the
# bottom that was originally used to keep track of the IP address and email
# the new one. This was used until the DDNS service replaced it.

# Location of log file
logfile=/home/pi/NEMETODE/logs/nemetode.log

# First, check for an active network connection and restart the
# networking service if necessary. This should make the connection
# robust to intermittent loss of broadband at the router.

# Ping four packets to router
ping -c4 192.168.1.254 > /dev/null

# Check if this was successful
if [ $? != 0 ]; then
    echo $(date) "> Lost network! Restarting..." >> ${logfile}
    sudo /etc/init.d/networking stop
    sleep 10
    sudo /etc/init.d/networking start
    sleep 60
fi

# Get current IP address
#CURRENT_IP=$(/usr/bin/wget -qO- http://ipecho.net/plain)

# Check against the previous IP address
#PREVIOUS_IP=$(cat /home/pi/NEMETODE/logs/ip.txt)

# If they are the same, do nothing
#if [ "$CURRENT_IP" == "$PREVIOUS_IP" ]; then
#    exit
#fi

#echo $(date) "> Found new IP address: " ${CURRENT_IP} >> ${logfile}

# IP address has changed: update the cached IP
#echo ${CURRENT_IP} > /home/pi/NEMETODE/logs/ip.txt

# Now send the new IP by email
#echo ${CURRENT_IP} | mail -s "[RaspberryPi] New IP address" nr@roe.ac.uk
