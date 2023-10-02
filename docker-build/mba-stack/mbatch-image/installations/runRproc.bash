#!/bin/bash

# (CLJ)
#
# runMbatch1.R makes a get request to MBA/MBA/JOBnext to see if there are pending jobs
# A is the output of runMBatch1.R, which is either the jobId as a string or "none"
#
# runMBatch2.R runs the actual R stastical analysis (a.k.a. the R function mbatchRunFromConfig)
# B is the output of runMBatch2.R, which is a string of either "success", "warning", or "error"
#
# runMBatch3.R does the post-run analysis. It looks if MBATCH_SUCCESS.txt exists, or creates
# MBATCH_FAILED.txt if it doesn't
#
# NOTE: I've left the R CMD BATCH executions here, but reverting to them will break the new structure

echo "start runRproc.bash"

baseDir=`pwd`
echo baseDir=${baseDir}

export mbaURL=$1

export rFile1=${baseDir}/runMBatch1.R
export rFile2=${baseDir}/runMBatch2.R
export rFile3=${baseDir}/runMBatch3.R
export rFileFinal=${baseDir}/runMBatchFinal.R
export rLogr=${baseDir}/runMBatch.rLog

echo 'Invoking R BEA_VERSION_TIMESTAMP'

# conda is a function, which is not propagated to bash scripts
# need to activate this so R can use it
echo "activate conda itself"
source ~/conda/etc/profile.d/conda.sh

# environment variables set in Dockerfile

# use this instead of looping inside R, since R leaks memory really badly
while true
do
	echo TIMING-START=$(date +"%Y_%m_%d_%H%M")
	echo Invoke R in 2 minutes
	sleep 120
	echo mbaURL=${mbaURL}
	echo rFile1=${rFile1}
	echo rFile2=${rFile2}
	echo rFile3=${rFile3}
	echo rFileFinal=${rFileFinal}
	echo rLogr=${rLogr}
	export A=$(Rscript --vanilla "$rFile1" "-mbaURL=$mbaURL" | tee 2>&1 "$rLogr")
	# A=$(R CMD BATCH --vanilla --args -mbaURL="$mbaURL" "$rFile1" "$rLogr")
	if [ "$A" != "none" ];
	then
		echo Found job "$A", now starting
		export jobLogr=/BEA/MBA/OUTPUT/${A}/log.rLog
		script -a -f -c 'Rscript --vanilla "$rFile2" "-jobID=$A"' "$jobLogr"
		script -a -f -c 'Rscript --vanilla "$rFile3" "-jobID=$A"' "$jobLogr"
		script -a -f -c 'Rscript --vanilla "$rFileFinal" "-jobID=$A" "-mbaURL=$mbaURL"' "$jobLogr"
		echo MBatch Complete
	else
	    echo No job found, now sleeping
	fi
	echo TIMING-DONE=$(date +"%Y_%m_%d_%H%M")
done

echo "finish runRproc.bash"
