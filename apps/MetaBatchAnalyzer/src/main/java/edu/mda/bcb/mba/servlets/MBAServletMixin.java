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

package edu.mda.bcb.mba.servlets;

import edu.mda.bcb.mba.utils.MBAUtils;
import edu.mda.bcb.mba.authorization.Authorization;
import edu.mda.bcb.mba.status.JobStatus;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Tod-Casasent
 */
public abstract class MBAServletMixin extends HttpServlet
{
	protected String mReturnType = "";
	protected boolean mCheckUserAuthorization = true;
	protected String mErrorFilename = null;
	protected File mErrorFile = null;
	
	public MBAServletMixin(String theReturnType, boolean theCheckUserAuthorization, Class theChildClass)
	{
		super();
		mReturnType = theReturnType;
		mCheckUserAuthorization = theCheckUserAuthorization;
		if (null!=theChildClass)
		{
			mErrorFilename = theChildClass.getName() + "_error.log";
		}
		else
		{
			mErrorFilename = null;
		}
		mErrorFile = null;
	}
	
	abstract protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception;
	
	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		try
		{
			log("version = " + edu.mda.bcb.mba.utils.MBAUtils.M_VERSION);
			String jobId = request.getParameter("jobId");
			log("passed in jobId is " + jobId);
			if (null!=jobId)
			{
				JobStatus.checkJobId(jobId);
			}
			if ((null!=jobId)&&(false!=mCheckUserAuthorization))
			{
				log("auth jobId is " + jobId);
				Authorization.userHasAccessException(this, request, jobId);
			}
			if ((null!=jobId)&&(null!=mErrorFilename))
			{
				File jobDir = new File(MBAUtils.M_OUTPUT, jobId);
				log("fileLocation is " + jobDir.getAbsolutePath());
				mErrorFile = new File(jobDir, mErrorFilename);
				if (mErrorFile.exists())
				{
					mErrorFile.delete();
				}
			}
			StringBuffer sb = new StringBuffer();
			internalProcess(request, sb);
			try (PrintWriter out = response.getWriter())
			{
				response.setContentType(mReturnType);
				response.setStatus(200);
				out.append(sb.toString());
			}
		}
		catch (Exception exp)
		{
			log("MBAServletMixin::processRequest failed", exp);
			response.setContentType("text;charset=UTF-8");
			response.setStatus(400);
			response.sendError(400);
			if (null!=mErrorFile)
			{
				Files.write(mErrorFile.toPath(), exp.getMessage().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			}
		}
	}
	
	protected void writeToError(String theMessage) throws IOException
	{
		if (null!=mErrorFile)
		{
			Files.write(mErrorFile.toPath(), theMessage.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		}
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
