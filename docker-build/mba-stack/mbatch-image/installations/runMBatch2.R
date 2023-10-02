# MBatch Copyright (c) 2011-2022 University of Texas MD Anderson Cancer Center
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
  Sys.getenv()
}

runStatus <- "success"
tryCatch({
  configFile <- file.path("/BEA/MBA/OUTPUT", jobID, "ZIP-RESULTS", "MBatchConfig.tsv")
  resultsDir <- file.path("/BEA/MBA/OUTPUT", jobID, "ZIP-RESULTS")
  dataDir <- file.path("/BEA/MBA/OUTPUT", jobID, "ZIP-DATA", "original")
  setwd(resultsDir)
  mbatchDF <- readAsGenericDataframe(file.path("/BEA/MBA/OUTPUT/mbatch.tsv"))
  mbatchRunFromConfig(theConfigFile=configFile,
                      theMatrixFile=file.path(dataDir, "matrix_data.tsv"),
                      theBatchesFile=file.path(dataDir, "batches.tsv"),
                      theZipDataDir=file.path("/BEA/MBA/OUTPUT", jobID, "ZIP-DATA"),
                      theZipResultsDir=resultsDir,
                      theNaStrings=c("null", "NA"),
                      theShaidyMapGen="/bcbsetup/ShaidyMapGen.jar",
                      theNgchmWidgetJs="/bcbsetup/ngchmWidget-min.js",
                      theShaidyMapGenJava="/usr/bin/java",
                      theNGCHMShaidyMem=mbatchDF$NGCHMShaidyMem, 
                      theRunPostFlag=TRUE)
}, error = function(err){
  message(paste("runMBatch2.R hit the error: ", err))
  runStatus <- "error"
  traceback()
}, finally = {
  # do not catch warnings, since then they act like errors and stop the process
  warnings()
  sessionFile <- file.path("/BEA/MBA/OUTPUT", jobID, "ZIP-RESULTS", "session_r.txt")
  writeSessionFile(sessionFile)
  cat(runStatus)
})
