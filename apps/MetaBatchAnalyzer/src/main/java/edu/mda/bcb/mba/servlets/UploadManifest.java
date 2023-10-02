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

package edu.mda.bcb.mba.servlets;

import edu.mda.bcb.mba.utils.MBAUtils;
import edu.mda.bcb.mba.status.JOB_STATUS;
import edu.mda.bcb.mba.status.JobStatus;
import edu.mda.bcb.mba.utils.ScanCheck;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author Tod-Casasent
 */
@MultipartConfig
@WebServlet(name = "UploadManifest", urlPatterns =
{
	"/UploadManifest"
})
public class UploadManifest extends MBAServletMixin
{

	public UploadManifest()
	{
		super("application/text;charset=UTF-8", true, UploadManifest.class);
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
		// return to user handled in parent
		String jobId = request.getParameter("jobId");
		JobStatus.checkJobId(jobId);
		String isAlternate = request.getParameter("isAlternate");
		ScanCheck.checkForYesNo(isAlternate);
		String dataset = request.getParameter("dataset");
		ScanCheck.checkForMetaCharacters(dataset);
		String dstype = request.getParameter("dstype");
		ScanCheck.checkForMetaCharacters(dstype);
		log("passed in jobId is " + jobId);
		log("isAlternate is " + isAlternate);
		log("dataset is " + dataset);
		log("dstype is " + dstype);
		String message = jobId;
		if (("gdc".equals(dstype))||("mw".equals(dstype)))
		{
			boolean isAlternateP = isAlternate.equals("YES");
			File jobDir = new File(MBAUtils.M_OUTPUT, jobId);
			String uploadDir = jobDir.getAbsolutePath();
			new File(uploadDir).mkdirs();
			String altStr = "PRI";
			if (isAlternateP)
			{
				altStr = "SEC";
			}
			File datasetDir = new File(uploadDir, altStr);
			datasetDir.mkdirs();
			File datasetFile = new File(datasetDir, "dataset.txt");
			try
			{
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(datasetFile)))
				{
					String [] tokens = dataset.split(" <> ", -1);
					for (String val : tokens)
					{
						bw.write(val);
						bw.newLine();
					}
				}
				if (isAlternateP)
				{
					if ("gdc".equals(dstype))
					{
						JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_SECONDARY_GDC_MANIFEST, request, this);
					}
					else if ("mw".equals(dstype))
					{
						JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_SECONDARY_MW_MANIFEST, request, this);
					}
				}
				else
				{
					if ("gdc".equals(dstype))
					{
						JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_PRIMARY_GDC_MANIFEST, request, this);
					}
					else if ("mw".equals(dstype))
					{
						JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_PRIMARY_MW_MANIFEST, request, this);
					}
				}
			}
			catch (Exception exp)
			{
				message = exp.getMessage();
				log("Problem in UploadManifest for job " + jobId + ". Error: " + exp.getMessage(), exp);
				throw new Exception("Problem in UploadManifest for job " + jobId + ". Error: " + exp.getMessage(), exp);
			}
		}
		else
		{
			message = "unknown manifest type '" + dstype + "'";
		}
		theBuffer.append(message);
	}
	
	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo()
	{
		return "Short description";
	}// </editor-fold>

}
