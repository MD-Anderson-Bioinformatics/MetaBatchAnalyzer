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

import edu.mda.bcb.mba.utils.MBAUtils;
import edu.mda.bcb.mba.servlets.MBAServletMixin;
import edu.mda.bcb.mba.status.JOB_STATUS;
import edu.mda.bcb.mba.status.JobStatus;
import edu.mda.bcb.mba.utils.ScanCheck;
import java.io.File;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Tod-Casasent
 */
@MultipartConfig
@WebServlet(name = "JOBMWDownload", urlPatterns =
{
	"/JOBMWDownload"
})
public class JOBMWDownload extends MBAServletMixin
{
	public JOBMWDownload()
	{
		super("application/text;charset=UTF-8", true, JOBMWDownload.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		////////////////////////////////////////////////////////////////////
		String jobId = request.getParameter("jobId");
		ScanCheck.checkForMetaCharacters(jobId);
		log("passed in jobId is " + jobId);
		JobStatus.checkJobId(jobId);
		String isAlternate = request.getParameter("isAlternate");
		ScanCheck.checkForYesNo(isAlternate);
		log("passed in isAlternate is " + isAlternate);
		File jobDir = new File(MBAUtils.M_OUTPUT, jobId);
		log("fileLocation is " + jobDir.getAbsolutePath());
		////////////////////////////////////////////////////////////////////
		String altStr = "PRI";
		if (isAlternate.equals("YES"))
		{
			altStr = "SEC";
		}
		// find subdir that starts with PRI or SEC
		File [] subdirs = jobDir.listFiles();
		File secPriDir = null;
		for (File sd : subdirs)
		{
			if (sd.isDirectory())
			{
				if (sd.getName().startsWith(altStr))
				{
					secPriDir = sd;
				}
			}
		}
		new File(secPriDir, "util").mkdirs();
		// copy HG38_Genes.tsv to secPriDir
		FileUtils.copyFile(new File(MBAUtils.M_UTILS, "HG38_Genes.tsv"), new File(new File(secPriDir, "util"), "HG38_Genes.tsv"));
		//
		File configFile = new File(secPriDir, "PROCESS.TXT");
		configFile.createNewFile();
		if (isAlternate.equals("YES"))
		{
			JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_SECONDARY_MW_WAIT, request, this);
		}
		else
		{
			JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_PRIMARY_MW_WAIT, request, this);
		}
		// status really comes from job status mbang rechecked
		theBuffer.append(jobId);
	}
}
