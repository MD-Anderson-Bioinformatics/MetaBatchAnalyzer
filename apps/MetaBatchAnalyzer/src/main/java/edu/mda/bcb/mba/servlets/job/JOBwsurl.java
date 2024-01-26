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
import edu.mda.bcb.mba.servlets.MBAproperties;
import edu.mda.bcb.mba.status.JobStatus;
import edu.mda.bcb.mba.utils.ScanCheck;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "JOBwsurl", urlPatterns =
{
	"/JOBwsurl"
})
public class JOBwsurl extends MBAServletMixin
{
	public JOBwsurl()
	{
		super("application/text;charset=UTF-8", true, JOBwsurl.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		ScanCheck.checkForMetaCharacters(jobId);
		log("passed in jobId is " + jobId);
		JobStatus.checkJobId(jobId);
		theBuffer.append(getURL(jobId, this, request.getScheme()));
	}

	static protected String getURL(String theJobId, HttpServlet theServlet, String theScheme) throws IOException
	{
		File jobDir = new File(MBAUtils.M_OUTPUT, theJobId);
		theServlet.log("jobDir is " + jobDir.getAbsolutePath());
		String url = "";
		if (new File(jobDir, "website.txt").exists())
		{
			List<String> wsLine = Files.readAllLines(new File(jobDir, "website.txt").toPath());
			// third line is website URL
			url = wsLine.get(2);
		}
		theServlet.log("url=" + url);
		// check if URL has new version of path
		if (!url.contains("/BEV/view"))
		{
			theServlet.log("reset URL for new BEV");
			// copied from buildUrlToWebsite in JobStatus
			url = theScheme + "://" + MBAproperties.getProperty("BEV_URL", theServlet) + "/MBA/BEV/view?id=" + theJobId + "&index=MBA_JOB&alg=PCA%2B&lvl1=BatchId";
			theServlet.log("reset URL=" + url);
		}
		return url;
	}
}
