#!/bin/bash
echo "start runGDCDownload.bash"

# like 10G or 64G
memsize=`grep -oP '(?<=GDC_MEMSIZE: ).*' /MBA/OUTPUT/gdc.properties`
mbaURL=$1
# anything but 'once' means repeat forever
repeat=once
echo memsize=${memsize}
echo mbaURL=${mbaURL}
echo repeat=${repeat}

echo "start loop in 15 seconds"
sleep 15
while true  
do  
  sleep 300  
  ./runJavaGDC.bash ${memsize} ${mbaURL} ${repeat}
  ./runJavaMWB.bash ${memsize} ${mbaURL} ${repeat}
done


echo "finish runGDCDownload.bash"

