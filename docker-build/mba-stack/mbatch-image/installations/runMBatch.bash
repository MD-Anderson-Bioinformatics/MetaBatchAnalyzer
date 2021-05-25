#!/bin/bash
echo "start runMBatch.bash"

mbaURL=$1
echo mbaURL=${mbaURL}

echo "runRproc.bash in 15 seconds BEA_VERSION_TIMESTAMP"
sleep 15
./runRproc.bash ${mbaURL}

echo "finish runMBatch.bash"

