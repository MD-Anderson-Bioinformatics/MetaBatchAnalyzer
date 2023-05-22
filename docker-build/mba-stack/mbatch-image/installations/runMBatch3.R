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

findCorrectedTsv <- function(theBaseDir)
{
  filepath <- NULL
  fileFound <- dir(theBaseDir, "corrected_matrix.tsv")
  if (length(fileFound)>0)
  {
    filepath <- file.path(theBaseDir, fileFound)
  }
  filepath
}

# buildSingleArchive(mbatchID, originalDataJsonFile, mbatchResultsDir, zipDir)
# String mbatchID = "MBATCH_ID_0000";
# String originalDataJsonFile = "/path/2020_03_12_1022/original_data.json";
# String mbatchResultsDir = "/path/2020_03_12_1022/MBatch/";
# String zipDir = "/path/2020_03_12_1022/";

runStatus <- "success"
tryCatch({
  mbatchID <- jobID
  zipDir <- file.path("/MBA/OUTPUT", jobID)
  resultDir <- file.path("/MBA/OUTPUT", jobID, "ZIP-RESULTS")
  dataDir <- file.path("/MBA/OUTPUT", jobID, "ZIP-DATA")
  message(paste("runMBatch3.R mbatchID: ", mbatchID))
  message(paste("runMBatch3.R zipDir: ", zipDir))
  message(paste("runMBatch3.R resultDir: ", resultDir))
  message(paste("runMBatch3.R dataDir: ", dataDir))
  buildSingleArchive(theResultDir=resultDir, theDataDir=dataDir, theZipDir=zipDir)
}, error = function(err){
  message(paste("runMBatch3.R hit the error: ", err))
  runStatus <- "error"
  traceback()
}, finally = {
  # do not catch warnings, since then they act like errors and stop the process
  warnings()
  cat(runStatus)
})
