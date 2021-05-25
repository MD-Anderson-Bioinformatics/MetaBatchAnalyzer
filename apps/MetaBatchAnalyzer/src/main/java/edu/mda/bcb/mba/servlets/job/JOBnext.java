// Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.mba.servlets.job;

import edu.mda.bcb.mba.servlets.MBAServletMixin;
import edu.mda.bcb.mba.status.JOB_STATUS;
import edu.mda.bcb.mba.status.JobStatus;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

// JOBnext USED IN C:\work\code\BatchEffects\docker\MBatchImage\installations\runMBatch1.R
// JOBnext USED IN GDCDownload edu.mda.bcb.gdc.download.DatasetConfig

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "JOBnext", urlPatterns =
{
	"/JOBnext"
})
public class JOBnext extends MBAServletMixin
{
	public JOBnext()
	{
		super("application/text;charset=UTF-8", true, JOBnext.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobType = request.getParameter("jobType");
		log("passed in jobType is '" + jobType + "'");
		String message = "Unknown job type '" + jobType + "'";
		// JobStatus.getWithJobStatusUpdate will find a job of the passed jobType and ...
		// set the message equal to the jobId. The message is then returned to the calling container and ...
		// initiates an Mbatch/GDCDownload process inside the container
		if ("MBATCH".equals(jobType))
		{
			message = JobStatus.getWithJobStatusUpdate(JOB_STATUS.MBATCHRUN_START_WAIT, JOB_STATUS.MBATCHRUN_ACCEPTED_WAIT, null, null, request, this);
		}
		else if ("GDCDLD".equals(jobType))
		{
			message = JobStatus.getWithJobStatusUpdate(JOB_STATUS.NEWJOB_PRIMARY_GDC_WAIT, JOB_STATUS.NEWJOB_PRIMARY_GDCRUN_WAIT,
					JOB_STATUS.NEWJOB_SECONDARY_GDC_WAIT, JOB_STATUS.NEWJOB_SECONDARY_GDCRUN_WAIT, request, this);

		}
		else if ("MWDDLD".equals(jobType))
		{
			message = JobStatus.getWithJobStatusUpdate(JOB_STATUS.NEWJOB_PRIMARY_MW_WAIT, JOB_STATUS.NEWJOB_PRIMARY_MWRUN_WAIT,
					JOB_STATUS.NEWJOB_SECONDARY_MW_WAIT, JOB_STATUS.NEWJOB_SECONDARY_MWRUN_WAIT, request, this);

		}
		theBuffer.append(message);
	}
}
