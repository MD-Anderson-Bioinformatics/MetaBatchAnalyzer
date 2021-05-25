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

import edu.mda.bcb.mba.authorization.Authorization;
import edu.mda.bcb.mba.servlets.MBAServletMixin;
import edu.mda.bcb.mba.servlets.MBAproperties;
import edu.mda.bcb.mba.status.JobStatus;
import java.util.Arrays;
import java.util.TreeSet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "JOBinfo", urlPatterns =
{
	"/JOBinfo"
})
public class JOBinfo extends MBAServletMixin
{
	public JOBinfo()
	{
		super("application/text;charset=UTF-8", true, JOBinfo.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		log("passed in jobId is " + jobId);
		// Pass in user tag and update it if the value has changed
		String newJobTag = request.getParameter("jobTag");
		// do not modify job owner, included for updating auths
		String jobOwner = request.getParameter("jobOwner");
		String newJobEmail = request.getParameter("jobEmail");
		JobStatus.setJobInfo(jobId, newJobTag, newJobEmail);
		//
		if (true==MBAproperties.isLoginAllowed(this))
		{
			TreeSet<String> jobAuthUsers = new TreeSet<>();
			TreeSet<String> jobAuthRoles = new TreeSet<>();
			String [] authUsers = request.getParameterValues("jobAuthUsers[]");
			log("passed in jobAuthUsers is " + Arrays.toString(authUsers));
			if (null!=authUsers)
			{
				jobAuthUsers.addAll(Arrays.asList(request.getParameterValues("jobAuthUsers[]")));
			}
			String [] authRoles = request.getParameterValues("jobAuthRoles[]");
			log("passed in jobAuthRoles is " + Arrays.toString(authRoles));
			if (null!=authRoles)
			{
				jobAuthRoles.addAll(Arrays.asList(request.getParameterValues("jobAuthRoles[]")));
			}
			Authorization.updateAuthorizationData(this, jobId, jobOwner, jobAuthUsers, jobAuthRoles);
		}
		// status really comes from job status mbang rechecked
		theBuffer.append(jobId);
	}

}
