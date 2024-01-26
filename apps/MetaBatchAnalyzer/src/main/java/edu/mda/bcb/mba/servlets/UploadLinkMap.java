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
package edu.mda.bcb.mba.servlets;

import edu.mda.bcb.mba.status.JobStatus;
import edu.mda.bcb.mba.utils.MBAUtils;
import edu.mda.bcb.mba.utils.ScanCheck;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

/**
 *
 * @author Tod-Casasent
 */
@MultipartConfig
@WebServlet(name = "UploadLinkMap", urlPatterns =
{
	"/UploadLinkMap"
})
public class UploadLinkMap extends MBAServletMixin
{

	public UploadLinkMap()
	{
		super("application/text;charset=UTF-8", true, UploadLinkMap.class);
	}

	/**
	 * Message returned from saveFileUpload to indicate success. All other
	 * values are interpreted as failure, likely providing an explanation as to
	 * the failure.
	 */
	private static final String successfulUpload = "FILE UPLOAD: SUCCESS";

	/**
	 * Message returned from matrixPostProcessing to indicate success. All other
	 * values are interpreted as failure, likely providing an explanation as to
	 * the failure.
	 */
	private static final String successfulPostProcessing = "POST PROCESS: SUCCESS";

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		// reutnr to user handled in parent
		String jobId = request.getParameter("jobId");
		JobStatus.checkJobId(jobId);
		String isAlternate = request.getParameter("isAlternate");
		ScanCheck.checkForYesNo(isAlternate);
		boolean isAlternateP = isAlternate.equals("YES");
		File jobDir = new File(MBAUtils.M_OUTPUT, jobId);
		File zipDataDir = new File(jobDir, "ZIP-DATA");
		File zipDataOriginalDir = new File(zipDataDir, "original");
		zipDataOriginalDir.mkdirs();
		final String savePath = new File(zipDataOriginalDir, (isAlternateP ? "ngchm_link_map2.tsv" : "ngchm_link_map.tsv")).getAbsolutePath();
		log("passed in jobId is " + jobId);
		log("passed in isAlternate is " + isAlternate);
		log("fileLocation is " + jobDir.getAbsolutePath());
		/**
		 * message will be the ultimate response text. A message equal to the
		 * jobId is interpreted as successful file upload, and anything else is
		 * interpreted as failure (with failure message).
		 */
		String message = jobId;
		try
		{
			String result1 = null;
			result1 = saveFileUpload(request.getPart("file"), savePath, this);
		}
		catch (IOException | ServletException exp)
		{
			message = "Problem in file upload for job " + jobId + ". Error: " + exp.getMessage();
			log("Problem in file upload for job " + jobId + ". Error: " + exp.getMessage(), exp);
			throw new Exception("Problem in file upload for job " + jobId + ". Error: " + exp.getMessage(), exp);
		}
		theBuffer.append(message);
	}

	// TODO: Evaluate whether its possible/wise to call the saveFileUpload in UploadBatch/UploadMatrix rather than have this method defined here
	public static String saveFileUpload(Part thePart, String theSavePath, HttpServlet theServlet)
	{
		String message = "";
		try
		{
			message = MBAUtils.uploadTextOnlyFile(thePart, theSavePath, theServlet, successfulUpload);
		}
		catch (FileNotFoundException fne)
		{
			message = "You either did not specify a file to upload or are trying to upload a file to a protected or nonexistent location. "
					+ fne.getMessage();
			theServlet.log("Problems during file upload. Error: " + fne.getMessage(), fne);
			//response.setStatus(400);
			//response.sendError(400, exp.getMessage());
		}
		catch (Exception exp)
		{
			message = exp.getMessage();
			theServlet.log("Problems during file upload. Error: " + exp.getMessage(), exp);
			//response.setStatus(400);
			//response.sendError(400, exp.getMessage());
		}
		return message;
	}
}
