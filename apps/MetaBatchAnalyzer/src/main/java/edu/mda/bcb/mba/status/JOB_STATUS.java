// Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.mba.status;

/**
 *
 * @author Tod-Casasent
 */
public enum JOB_STATUS
{
	// Starting Process for Primary Data
	NEWJOB_START("NEWJOB_START", "Waiting for new job data setup."),
	// GDC Download Statuses
	NEWJOB_PRIMARY_GDC_MANIFEST("NEWJOB_PRIMARY_GDC_MANIFEST", "Primary GDC Manifest Files Uploaded."),
	NEWJOB_PRIMARY_GDC_WAIT("NEWJOB_PRIMARY_GDC_WAIT", "Primary GDC Download Queued."),
	NEWJOB_PRIMARY_GDCRUN_WAIT("NEWJOB_PRIMARY_GDCRUN_WAIT", "Primary GDC Download in Progress."),
	// MW Download Statuses
	NEWJOB_PRIMARY_MW_MANIFEST("NEWJOB_PRIMARY_MW_MANIFEST", "Primary Metabolomics Workbench Manifest Files Uploaded."),
	NEWJOB_PRIMARY_MW_WAIT("NEWJOB_PRIMARY_MW_WAIT", "Primary Metabolomics Workbench Download Queued."),
	NEWJOB_PRIMARY_MWRUN_WAIT("NEWJOB_PRIMARY_MWRUN_WAIT", "Primary Metabolomics Workbench Download in Progress."),
	// User Data Statuses
	NEWJOB_PRIMARY_USER_MATRIX("NEWJOB_PRIMARY_USER_MATRIX", "Primary Matrix Data Uploaded. Waiting for Batch Data."),
	// Starting Process for Secondary Data
	NEWJOB_PRIMARY_DONE("NEWJOB_PRIMARY_DONE", "Primary Data Available. Waiting for Secondary Data."),
	// GDC Download Statuses
	NEWJOB_SECONDARY_GDC_MANIFEST("NEWJOB_SECONDARY_GDC_MANIFEST", "Secondaryd GDC Manifest Files Uploaded."),
	NEWJOB_SECONDARY_GDC_WAIT("NEWJOB_SECONDARY_GDC_WAIT", "Secondary Matrix GDC Download Queued"),
	NEWJOB_SECONDARY_GDCRUN_WAIT("NEWJOB_SECONDARY_GDCRUN_WAIT", "Secondary Matrix GDC Download in Progress"),
	// MW Download Statuses
	NEWJOB_SECONDARY_MW_MANIFEST("NEWJOB_SECONDARY_MW_MANIFEST", "Secondary Metabolomics Workbench Manifest Files Uploaded."),
	NEWJOB_SECONDARY_MW_WAIT("NEWJOB_SECONDARY_MW_WAIT", "Secondary Metabolomics Workbench Download Queued."),
	NEWJOB_SECONDARY_MWRUN_WAIT("NEWJOB_SECONDARY_MWRUN_WAIT", "Secondary Metabolomics Workbench Download in Progress."),
	// User Data Statuses
	NEWJOB_SECONDARY_USER_MATRIX("NEWJOB_SECONDARY_USER_MATRIX", "Secondary Matrix Data Uploaded. Waiting for Batch Data."),
	// Finishing Process for Secondary Data
	NEWJOB_SECONDARY_DONE("NEWJOB_SECONDARY_DONE", "Secondary Data Available."),
	// Other states
	NEWJOB_FAILURE("NEWJOB_FAILURE", "Data Setup Failed."),
	NEWJOB_DONE("NEWJOB_DONE", "Data Setup Complete. Ready for MBatch Configuration."),
	
	MBATCHCONFIG_START("MBATCHCONFIG_START", "MBatch Configuration in Process"),
	MBATCHCONFIG_END("MBATCHCONFIG_END", "MBatch Configuration Complete"),
	
	MBATCHRUN_START_WAIT("MBATCHRUN_START_WAIT", "MBatch Run Queued"),
	MBATCHRUN_ACCEPTED_WAIT("MBATCHRUN_ACCEPTED_WAIT", "MBatch Run Accepted for Processing"),
	MBATCHRUN_RUNNING_WAIT("MBATCHRUN_RUNNING_WAIT", "MBatch Run in Progress"),
	MBATCHRUN_END_SUCCESS("MBATCHRUN_END_SUCCESS", "MBatch Run Finished Successfully"),
	MBATCHRUN_END_COMPLETED("MBATCHRUN_END_COMPLETED", "MBatch Run Completed (no results) Successfully"),
	MBATCHRUN_END_FAILURE("MBATCHRUN_END_FAILURE", "MBatch Run Failed");

	public final String mStatus;
	public final String mReport;

	JOB_STATUS(String theStatus, String theReportString)
	{
		this.mStatus = theStatus;
		this.mReport = theReportString;
	}

	static public JOB_STATUS StringToEnum(String theStatus)
	{
		JOB_STATUS result = null;
		for (JOB_STATUS status : JOB_STATUS.values())
		{
			if (status.mStatus.equals(theStatus))
			{
				result = status;
			}
		}
		return result;
	}

}
