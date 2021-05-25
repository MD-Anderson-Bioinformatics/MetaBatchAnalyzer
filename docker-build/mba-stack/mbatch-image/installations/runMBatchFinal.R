# MBatch Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
#
# This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
#
# MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
# MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

library(MBatch)
library(MBatchUtils)
library(httr)
  
message("runMBatchFinal.R BEA_VERSION_TIMESTAMP")

#jobID and mbaURL come from commandArgs()
jobID <- NULL
message(paste("commandArgs():", commandArgs(), sep="", collapse="\n"))

for( myStr in commandArgs() )
{
  message("runMBatchFinal:: processing command arg:", myStr)
  if (length(grep("^-jobID=", myStr))>0)
  {
    jobID <- substring(myStr, nchar("-jobID=")+1) 
    message("runMBatchFinal:: found argument ", jobID)
  }else if (length(grep("^-mbaURL=", myStr))>0)
  {
    mbaURL <- substring(myStr, nchar("-mbaURL=")+1) 
    message("runMBatchFinal:: found argument ", mbaURL)
  }
}

successFile <- file.path("/MBA/OUTPUT", jobID, "ZIP-RESULTS", "MBATCH_SUCCESS.txt")
completedFile <- file.path("/MBA/OUTPUT", jobID, "ZIP-RESULTS", "MBATCH_COMPLETED.txt")
failFile <- file.path("/MBA/OUTPUT", jobID, "ZIP-RESULTS", "MBATCH_FAILED.txt")
runStatus <- "success"
tryCatch({
	if (file.exists(successFile))
	{
	  message("runMBatchFinal::  JOBupdate sending MBATCHRUN_END_SUCCESS for job '", jobID, "'")
	  jobResponse <- GET(url=paste(mbaURL, "/MBA/MBA/JOBupdate?jobId=", jobID, "&status=MBATCHRUN_END_SUCCESS", sep=""))
	  message("runMBatchFinal::  JOBupdate response '", content(jobResponse, "text"), "'")
	} else if (file.exists(completedFile))
	{
	  message("runMBatchFinal::  JOBupdate sending MBATCHRUN_END_COMPLETED for job '", jobID, "'")
	  jobResponse <- GET(url=paste(mbaURL, "/MBA/MBA/JOBupdate?jobId=", jobID, "&status=MBATCHRUN_END_COMPLETED", sep=""))
	  message("runMBatchFinal::  JOBupdate response '", content(jobResponse, "text"), "'")
	} else {
	  message("runMBatchFinal::  file not found: ", successFile)
	  message("runMBatchFinal::  JOBupdate sending MBATCHRUN_END_FAILURE for job '", jobID, "'")
	  message("runMBatchFinal::  make file: ", failFile)
	  file.create(failFile)
	  jobResponse <- GET(url=paste(mbaURL, "/MBA/MBA/JOBupdate?jobId=", jobID, "&status=MBATCHRUN_END_FAILURE", sep=""))
	  message("runMBatchFinal::  JOBupdate response '", content(jobResponse, "text"), "'")
	}
}, warning = function(war){
  message(paste("runMBatchFinal.R hit the Warning: ", war))
  runStatus <- "warning"
  traceback()
}, error = function(err){
  message(paste("runMBatchFinal.R hit the error: ", err))
  runStatus <- "error"
  traceback()
}, finally = {
  cat(runStatus)
})


# if(file.exists(file.path(getwd(), "runMBatch.rLog")))
# {
#   file.copy(file.path(getwd(),"runMBatch.rLog"), 
#             file.path("/MBA/OUTPUT", jobID))
# }else
# {
#   message("Did not copy runMBatch.rLog to /MBA/OUTPUT/jobID because file not found")
# }
#
