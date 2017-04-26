#!/bin/bash
#
# Script to copy, process and delete previous night's videos,
# compress the raw data, switch off the main computer.

# Log file location
logfile=/home/pi/NEMETODE/logs/nemetode.log
logDiskUsage=/home/pi/NEMETODE/logs/used.txt
logDiskFree=/home/pi/NEMETODE/logs/free.txt
logDiskFreeE=/home/pi/NEMETODE/logs/free_main.txt
logDiskSizeE=/home/pi/NEMETODE/logs/size_main.txt

# Local videos location
videopath=/home/pi/NEMETODE/videos

yyyy=$(date --date="yesterday" +"%Y")
mm=$(date --date="yesterday" +"%m")
dd=$(date --date="yesterday" +"%d")

# Command to copy all the previous night's footage off the main
# computer to a local file system
remote_ip="192.168.1.97"

# Make local directory on the Pi to store the latest videos
local_dir=${videopath}"/"${yyyy}"/"${yyyy}${mm}"/"${yyyy}${mm}${dd}
remote_dir="E:\\"${yyyy}"\\"${yyyy}${mm}"\\"${yyyy}${mm}${dd}"\\"
echo $(date) "> Creating local directory " ${local_dir} >> ${logfile}
mkdir -p ${local_dir}
# Set permission on the new directory such that we can delete contents
chmod 0777 ${local_dir}

# Copy all of last night's videos and other files to the local directory
echo $(date) "> Copying video files from remote directory " ${remote_dir} >> ${logfile}
scp CJ1@${remote_ip}:${remote_dir}* ${local_dir}
echo $(date) "> Got " $(ls ${local_dir}/*.avi | wc -l) " video clips" >> ${logfile}

# Get disk usage for main computer
mainCompDriveE=$(ssh CJ1@192.168.1.97 "echo '' | wmic logicaldisk get size,freespace,caption" | grep E:)
echo $mainCompDriveE | awk '{print $2}' > ${logDiskFreeE}
echo $mainCompDriveE | awk '{print $3}' > ${logDiskSizeE}

# Check if the meteor drive needs to be defragmented
defrag=$(ssh CJ1@192.168.1.97 'defrag e: /a')
echo $(date) "> Defrag status report: " $defrag >> ${logfile}
# Check if 'You should defragment this volume' appears anywhere in the defrag status:
if [[ $defrag == *"You should defragment this volume"* ]]
then
  echo $(date) "> Defragmenting the main computer E: drive" >> ${logfile}
  defragPerf=$(ssh CJ1@192.168.1.97 'defrag e:')
  echo $(date) "> Defrag performance report: " $defragPerf >> ${logfile}
fi

# Issue command to shut down the main computer
echo $(date) "> Shutting down the main computer" >> ${logfile}
ssh CJ1@${remote_ip} 'shutdown -s'

# Make compressed video from the raw video for each clip
cd ${local_dir}

# Purge the files beginning T*, which are the temporary circular buffers
rm T*

echo $(date) "> Compressing the video files" >> ${logfile}

# Following command prevents $AVI from resolving to '*.avi' if there are no
# avi files in the directory.
shopt -s nullglob

for AVI in *.avi
do

# Get file name with '_NR.avi' extension stripped off
NAME=$(echo ${AVI} | sed 's/_NR.avi//')

# Make compressed versions of the raw footage
avconv -i ${NAME}_NR.avi -vcodec libx264 -crf 0 -an -vf transpose=1 ${NAME}.mp4
avconv -i ${NAME}_NR.avi -vcodec wmv2    -crf 0 -an -vf transpose=1 ${NAME}.wmv
# Make thumbnail image
convert ${NAME}_NRP.jpg -transpose -flop ${NAME}.jpg

# Zip up the raw data files
mkdir ${NAME}
mv ${NAME}_* ${NAME}
zip -r ${NAME}.zip ${NAME}
rm -r ${NAME}

done

# Get the Raspberry Pi disk usage and dump to file for importing to website
cd ${videopath}
df -hBk . | grep /dev/root | awk '{print $3}' | sed 's/K//' > ${logDiskUsage}
df -hBk . | grep /dev/root | awk '{print $4}' | sed 's/K//' > ${logDiskFree}

echo $(date) "> Complete!" >> ${logfile}
