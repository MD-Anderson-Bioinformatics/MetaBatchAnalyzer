#!/bin/bash
echo "start runJavaGDC.bash"

baseDir=`pwd`
echo baseDir=${baseDir}

# 64G
memsize=$1
mbaURL=$2
# anything but 'once' means repeat forever
repeat=$3
echo memsize=${memsize}
echo mbaURL=${mbaURL}
echo repeat=${repeat}

THIS_DIR=`pwd`
# this will look for PROCESS.TXT to find subdir in /MBA/OUTPUT/<jobid> to process
# remove PROCESS.TXT and replace with SUCCCESS.TXT or FAILED.TXT on completion
java -Xmx${memsize} -cp "${THIS_DIR}/*" edu.mda.bcb.stdmwutils.MWStack /MBA/OUTPUT ${mbaURL} ${repeat}

echo "finish runJavaGDC.bash"
