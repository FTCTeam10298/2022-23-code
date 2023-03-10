#!/bin/bash

# See https://developer.android.com/studio/command-line/logcat#alternativeBuffers
BUFFERS="radio events main system crash"
DATE=`date "+%Y-%m-%d_%H.%M.%S_%Z"`
ADB=~/Library/Android/sdk/platform-tools/adb

set -e

DEVICES=`$ADB devices  | tail +2  | cut -f 1`

echo "DUMPING THE FOLLOWING DEVICES: "
echo "$DEVICES"
echo "--------------------------------"

for DEVICE in $DEVICES
do
  echo "DUMPING DEVICE $DEVICE"
  TAG="`whoami`-$DEVICE-$DATE"
  DEST_DIR="android-logs/$TAG"
  mkdir -p $DEST_DIR


  for BUFFER in $BUFFERS
  do
    DEST_FILE="$DEST_DIR/$DEVICE-$DATE-$BUFFER.log"
    echo "   Dumping $BUFFER to $DEST_FILE"
    $ADB -s $DEVICE  logcat -b $BUFFER -v descriptive -v year -v uid -d > $DEST_FILE
    echo "    `cat $DEST_FILE | wc -l` LINES"

  done
done



