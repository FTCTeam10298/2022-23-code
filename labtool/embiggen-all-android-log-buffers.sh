#!/bin/bash

# Historically, it looks like this:

# stu@Jan-Hus ~ % ~/Library/Android/sdk/platform-tools/adb -s HT7921A02942 logcat -g
# main: ring buffer is 256 KiB (204 KiB consumed), max entry is 5120 B, max payload is 4068 B
# system: ring buffer is 256 KiB (27 KiB consumed), max entry is 5120 B, max payload is 4068 B
# crash: ring buffer is 256 KiB (9 KiB consumed), max entry is 5120 B, max payload is 4068 B
# kernel: ring buffer is 256 KiB (0 B consumed), max entry is 5120 B, max payload is 4068 B

# stu@Jan-Hus ~ % ~/Library/Android/sdk/platform-tools/adb -s 014AY1RRXQ logcat -g
# main: ring buffer is 256 KiB (244 KiB consumed), max entry is 5120 B, max payload is 4068 B
# system: ring buffer is 256 KiB (253 KiB consumed), max entry is 5120 B, max payload is 4068 B
# crash: ring buffer is 256 KiB (6 KiB consumed), max entry is 5120 B, max payload is 4068 B
# kernel: ring buffer is 256 KiB (0 B consumed), max entry is 5120 B, max payload is 4068 B

ADB=~/Library/Android/sdk/platform-tools/adb

set -e

DEVICES=`$ADB devices  | tail +2  | cut -f 1`

echo "FOUND THE FOLLOWING DEVICES: "
echo "$DEVICES"
echo "--------------------------------"

for DEVICE in $DEVICES
do
  echo "SETTING LOG BUFFER SIZE FOR DEVICE $DEVICE"

$ADB -s $DEVICE  logcat -G 10M
$ADB -s $DEVICE  logcat -g
done



