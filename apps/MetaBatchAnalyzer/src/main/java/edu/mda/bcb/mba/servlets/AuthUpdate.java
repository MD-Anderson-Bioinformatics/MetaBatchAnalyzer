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
import edu.mda.bcb.mba.authorization.UserAndRoleData;
import edu.mda.bcb.mba.utils.PropertiesEsc;
import edu.mda.bcb.mba.utils.ScanCheck;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.realm.GenericPrincipal;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "AuthUpdate", urlPatterns =
{
	"/AuthUpdate"
})
public class AuthUpdate extends HttpServlet
{
	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	synchronized protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		// TODO: replace synchronized if needed to speed up login process
		try
		{
			ScanCheck.checkForSecurity(request);
			try
			{
				// *******************************************************************
				// Update User List and Role Information
				log("request.getRemoteUser() = " + request.getRemoteUser());
				String userName = getUserName(request);
				if (null==userName)
				{
					log("nobody logged in");
				}
				else
				{
					PropertiesEsc props = new PropertiesEsc();
					if (new File(MBAUtils.M_PROPS, "user.properties").exists())
					{
						try (FileInputStream is = new FileInputStream(new File(MBAUtils.M_PROPS, "user.properties")))
						{
							props.loadFromXML(is);
						}
					}
					props.setProperty(userName, getUserRoleString(request));
					try (FileOutputStream os = new FileOutputStream(new File(MBAUtils.M_PROPS, "user.properties")))
					{
						props.storeToXML(os, "user properties");
					}
					log("AuthUpdate::processRequest updateUserAndRoleData");
					UserAndRoleData.updateUserAndRoleData(props);
				}
			}
			catch (Exception exp)
			{
				log("AuthUpdate::processRequest failed", exp);
			}
			// *******************************************************************
			// redirect user to main index
			String url = request.getHeader("referer");
			log("AuthOut referer=" + url);
			if ((null==url)||("".equals(url)))
			{
				url = "/MBA/MBA/index.html";
			}
			String urlWithSessionID = response.encodeRedirectURL(url);
			response.sendRedirect( urlWithSessionID );
		}
		catch(Exception exp)
		{
			log("AuthUpdate::processRequest failed", exp);
			response.setStatus(400);
			response.sendError(400);
		}
	}
	
	static public String getUserName(HttpServletRequest request)
	{
		String result = null;
		Principal p = request.getUserPrincipal();
		if (null!=p)
		{
			result = p.getName();
		}
		return result;
	}
	
	static public TreeSet<String> getUserRoles(HttpServletRequest request)
	{
		Principal p = request.getUserPrincipal();
		TreeSet<String> roles = new TreeSet<>();
		if (null!=p)
		{
			System.out.println("request.getUserPrincipal().getClass() = " + request.getUserPrincipal().getClass());
			GenericPrincipal gp = (GenericPrincipal)p;
			for (String role : gp.getRoles())
			{
				roles.add(role);
			}
		}
		return roles;
	}
	
	static public String getUserRoleString(HttpServletRequest request)
	{
		Principal p = request.getUserPrincipal();
		String roleString = null;
		if (null!=p)
		{
			System.out.println("request.getUserPrincipal().getClass() = " + request.getUserPrincipal().getClass());
			GenericPrincipal gp = (GenericPrincipal)p;
			for (String role : gp.getRoles())
			{
				if (null==roleString)
				{
					roleString = role;
				}
				else
				{
					roleString = roleString + "|" + role;
				}
			}
		}
		return roleString;
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
