# MBatch Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
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

message("runMBatch1.R BEA_VERSION_TIMESTAMP")

### received in commandArgs()
message(commandArgs())
mbaURL <- NULL

message(paste("commandArgs():", commandArgs(), sep="", collapse="\n"))

for( myStr in commandArgs() )
{
	message("processing command arg:", myStr)
	if (length(grep("^-mbaURL=", myStr))>0)
	{
		mbaURL <- substring(myStr, nchar("-mbaURL=")+1) 
		message("found argument ", mbaURL)
	}
}

message("mbaURL= '", mbaURL, "'")
jobResponse <- GET(url=paste(mbaURL, "/MBA/MBA/JOBnext?jobType=MBATCH", sep=""))
message(jobResponse)
jobID <- content(jobResponse, "text")
message("mbaURL response '", jobID, "'")

if ("none"==jobID)
{
  cat("none")
} else {
	message("jobID is '", jobID, "'")
	message("mbaURL= '", mbaURL, "'")
	jobResponse <- GET(url=paste(mbaURL, "/MBA/MBA/JOBupdate?jobId=", jobID, "&status=MBATCHRUN_RUNNING_WAIT", sep=""))
	message("mbaURL JOBupdate response '", content(jobResponse, "text"), "'")
	cat(toString(jobID))
}
