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

message("runMBatch2.R BEA_VERSION_TIMESTAMP")

#jobID comes from commandArgs()
jobID <- NULL
message(paste("commandArgs():", commandArgs(), sep="", collapse="\n"))

for( myStr in commandArgs() )
{
  message("processing command arg:", myStr)
  if (length(grep("^-jobID=", myStr))>0)
  {
    jobID <- substring(myStr, nchar("-jobID=")+1) 
    message("found argument ", jobID)
  }
}

writeSessionFile <- function(theFile)
{
  on.exit(sink())
  sink(theFile)
  print(sessionInfo())
  cat("\n")
  cat("Java Version\n")
  library(rJava)
  .jinit()
  cat(capture.output(J("java.lang.System")$getProperty("java.version")))
}

runStatus <- "success"
tryCatch({
  configFile <- file.path("/MBA/OUTPUT", jobID, "ZIP-RESULTS", "MBatchConfig.tsv")
  resultsDir <- file.path("/MBA/OUTPUT", jobID, "ZIP-RESULTS")
  dataDir <- file.path("/MBA/OUTPUT", jobID, "ZIP-DATA", "original")
  setwd(resultsDir)
  mbatchDF <- readAsGenericDataframe(file.path("/MBA/OUTPUT/mbatch.tsv"))
  mbatchRunFromConfig(theConfigFile=configFile,
                      theDataDir=dataDir,
                      theOutputDir=resultsDir,
                      theNaStrings=c("null", "NA"),
					  theShaidyMapGen="/bcb_install/ShaidyMapGen.jar",
                                          theNgchmWidgetJs="/bcb_install/ngchmWidget-min.js",
					  theShaidyMapGenJava="/usr/bin/java",
					  theNGCHMShaidyMem=mbatchDF$NGCHMShaidyMem, 
					  thePCAMem=mbatchDF$PCAMem, 
					  theBoxplotMem=mbatchDF$BoxplotMem,
					  theRunPostFlag=TRUE)
  sessionFile <- file.path("/MBA/OUTPUT", jobID, "ZIP-RESULTS", "session_r.txt")
  writeSessionFile(sessionFile)
}, warning = function(war){
  message(paste("runMBatch2.R hit the Warning: ", war))
  # a warning is still a success, and we don't want it to fail because of that
  runStatus <- "success"
  traceback()
}, error = function(err){
  message(paste("runMBatch2.R hit the error: ", err))
  runStatus <- "error"
  traceback()
}, finally = {
  cat(runStatus)
})
