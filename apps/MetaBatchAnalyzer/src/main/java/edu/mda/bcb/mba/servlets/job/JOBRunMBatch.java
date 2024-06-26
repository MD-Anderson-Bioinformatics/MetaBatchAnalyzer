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

package edu.mda.bcb.mba.servlets.job;

import edu.mda.bcb.mba.servlets.MBAServletMixin;
import edu.mda.bcb.mba.status.JOB_STATUS;
import edu.mda.bcb.mba.status.JobStatus;
import edu.mda.bcb.mba.utils.ScanCheck;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "JOBRunMBatch", urlPatterns =
{
	"/JOBRunMBatch"
})
public class JOBRunMBatch extends MBAServletMixin
{
	public JOBRunMBatch()
	{
		super("application/text;charset=UTF-8", true, JOBRunMBatch.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		ScanCheck.checkForMetaCharacters(jobId);
		log("passed in jobId is " + jobId);
		JobStatus.checkJobId(jobId);
		JobStatus.setJobStatus(jobId, JOB_STATUS.MBATCHCONFIG_END, request, this);
		JobStatus.setJobStatus(jobId, JOB_STATUS.MBATCHRUN_START_WAIT, request, this);
		theBuffer.append(jobId);
	}
}
