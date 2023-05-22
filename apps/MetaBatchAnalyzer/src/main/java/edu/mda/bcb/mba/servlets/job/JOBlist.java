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

import edu.mda.bcb.mba.servlets.AuthUpdate;
import edu.mda.bcb.mba.servlets.MBAServletMixin;
import edu.mda.bcb.mba.status.JobStatus;
import java.util.TreeSet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "JOBlist", urlPatterns =
{
	"/JOBlist"
})
public class JOBlist extends MBAServletMixin
{
	public JOBlist()
	{
		super("application/json;charset=UTF-8", true, JOBlist.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		// TODO: jobStatus.readProperties(); would refresh the job list from ...
		// the xml before returning the results to user. This also makes the user wait ...
		// for possibly no benefit.
		theBuffer.append("[\n");
		String username = AuthUpdate.getUserName(request);
		TreeSet<String> roles = AuthUpdate.getUserRoles(request);
		JobStatus.buildJobStatusList(this, theBuffer, username, roles);
		theBuffer.append("\n]\n");
	}
}
