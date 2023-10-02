// Copyright (c) 2011-2022 University of Texas MD Anderson Cancer Center
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

import edu.mda.bcb.mba.utils.MBAUtils;
import edu.mda.bcb.mba.servlets.MBAServletMixin;
import edu.mda.bcb.mba.status.JobStatus;
import edu.mda.bcb.mba.utils.ScanCheck;
import java.io.File;
import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "JOBdelete", urlPatterns =
{
	"/JOBdelete"
})
public class JOBdelete extends MBAServletMixin
{
	public JOBdelete()
	{
		super("application/text;charset=UTF-8", true, JOBdelete.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		ScanCheck.checkForMetaCharacters(jobId);
		log("passed in jobId is " + jobId);
		JobStatus.checkJobId(jobId);
		if ((jobId!=null)&&!("".equals(jobId)))
		{
			File jobDir = new File(MBAUtils.M_OUTPUT, jobId);
			boolean success = deleteWebsiteFiles(jobId, this);
			if(true==success)
			{
				success = FileUtils.deleteQuietly(jobDir);
				if(true==success)
				{
					JobStatus.deleteJob(jobId, this);
					theBuffer.append("Successfully deleted " + jobId);
				}
				else
				{
					theBuffer.append("Unable to delete " + jobId);
				}
			}
			else
			{
				theBuffer.append("Unable to delete website directory " + jobId);
			}
		}
		else
		{
			theBuffer.append("No job specified");
		}
	}
	
	protected boolean deleteWebsiteFiles(String theJobId, JOBdelete theLog) throws IOException
	{
		boolean success = true;
		File [] files = new File(MBAUtils.M_WEBSITE).listFiles();
		if (null!=files)
		{
			for (File myFile : files)
			{
				if (myFile.getName().startsWith(theJobId))
				{
					theLog.log("deleteWebsiteDirs " + myFile.getAbsolutePath());
					boolean test = FileUtils.deleteQuietly(myFile);
					if (test==false)
					{
						success = false;
					}
				}
			}
		}
		return success;
	}

}
