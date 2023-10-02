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

package edu.mda.bcb.mba.authorization;

import edu.mda.bcb.mba.servlets.AuthUpdate;
import edu.mda.bcb.mba.utils.MBAUtils;
import edu.mda.bcb.mba.servlets.MBAproperties;
import edu.mda.bcb.mba.utils.PropertiesEsc;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author Tod-Casasent
 */
public class Authorization
{
	static private boolean mIsRead = false;
	static private HashMap<String, String> mJobsToOwner =  new HashMap<>();
	static private HashMap<String, TreeSet<String>> mJobsToUsers =  new HashMap<>();
	static private HashMap<String, TreeSet<String>> mJobsToRoles =  new HashMap<>();
	
	private Authorization()
	{
		
	}
	
	synchronized static public TreeSet<String> getJobRoles(HttpServlet theServlet, String theJobId) throws IOException
	{
		TreeSet<String> result = new TreeSet<>();
		if (true==MBAproperties.isLoginAllowed(theServlet))
		{
			//theServlet.log("getJobRoles theJobId=" + theJobId);
			if (false==mIsRead)
			{
				readAuthorizationData(theServlet);
			}
			TreeSet<String> tmp = mJobsToRoles.get(theJobId);
			if (null!=tmp)
			{
				result.addAll(tmp);
			}
		}
		return result;
	}
	
	synchronized static public TreeSet<String> getJobUsers(HttpServlet theServlet, String theJobId) throws IOException
	{
		TreeSet<String> result = new TreeSet<>();
		if (true==MBAproperties.isLoginAllowed(theServlet))
		{
			//theServlet.log("getJobUsers theJobId=" + theJobId);
			if (false==mIsRead)
			{
				readAuthorizationData(theServlet);
			}
			TreeSet<String> tmp = mJobsToUsers.get(theJobId);
			if (null!=tmp)
			{
				result.addAll(tmp);
			}
		}
		return result;
	}
	
	static public String treeSetToString(TreeSet<String> theSet) throws IOException
	{
		String result = null;
		for(String data : theSet)
		{
			if (null==result)
			{
				result = data;
			}
			else
			{
				result = result + "|" + data;
			}
		}
		if (null==result)
		{
			result = "";
		}
		return result;
	}
	
	static public TreeSet<String> stringToTreeSet(String theString) throws IOException
	{
		TreeSet<String> result = new TreeSet<>();
		result.addAll(Arrays.asList(theString.split("\\|", -1)));
		return result;
	}
	
	synchronized static public void removeAuthorizationData(HttpServlet theServlet, String theJobId) throws IOException
	{
		if (true==MBAproperties.isLoginAllowed(theServlet))
		{
			if (false==mIsRead)
			{
				readAuthorizationData(theServlet);
			}
			if (MBAproperties.isLoginAllowed(theServlet))
			{
				//theServlet.log("removeAuthorizationData version = " + edu.mda.bioinfo.mba.servlets.MBAUtils.M_VERSION);
				// update maps with new information
				mJobsToUsers.remove(theJobId+ ".USERS");
				mJobsToRoles.remove(theJobId+ ".ROLES");
				mJobsToOwner.remove(theJobId+ ".OWNER");
				PropertiesEsc props = new PropertiesEsc();
				for (Entry<String, TreeSet<String>> myData : mJobsToUsers.entrySet())
				{
					props.setProperty(myData.getKey()+ ".USERS", treeSetToString(myData.getValue()));
				}
				for (Entry<String, TreeSet<String>> myData : mJobsToRoles.entrySet())
				{
					props.setProperty(myData.getKey()+ ".ROLES", treeSetToString(myData.getValue()));
				}
				for (Entry<String, String> myData : mJobsToOwner.entrySet())
				{
					props.setProperty(myData.getKey()+ ".OWNER", myData.getValue());
				}
				try (FileOutputStream os = new FileOutputStream(new File(MBAUtils.M_PROPS, "auth.properties")))
				{
					props.storeToXML(os, "authorization properties");
				}
			}
			else
			{
				//theServlet.log("updateAuthorizationData disabled/skipped");
			}
		}
	}
	
	
	synchronized static public void updateAuthorizationData(HttpServlet theServlet, String theJobId, String theJobOwner, 
			TreeSet<String> theUsers, TreeSet<String> theRoles) throws FileNotFoundException, IOException
	{

		if (true==MBAproperties.isLoginAllowed(theServlet))
		{
			if (false==mIsRead)
			{
				readAuthorizationData(theServlet);
			}
			if (MBAproperties.isLoginAllowed(theServlet))
			{
				//theServlet.log("updateAuthorizationData version = " + edu.mda.bioinfo.mba.servlets.MBAUtils.M_VERSION);
				//theServlet.log("theJobId = " + theJobId);
				long start = System.currentTimeMillis();
				// update maps with new information
				mJobsToUsers.put(theJobId, theUsers);
				mJobsToRoles.put(theJobId, theRoles);
				mJobsToOwner.put(theJobId, theJobOwner);
				// add job and users to properties
				PropertiesEsc props = new PropertiesEsc();
				for (Entry<String, TreeSet<String>> myData : mJobsToUsers.entrySet())
				{
					props.setProperty(myData.getKey()+ ".USERS", treeSetToString(myData.getValue()));
				}
				for (Entry<String, TreeSet<String>> myData : mJobsToRoles.entrySet())
				{
					props.setProperty(myData.getKey()+ ".ROLES", treeSetToString(myData.getValue()));
				}
				for (Entry<String, String> myData : mJobsToOwner.entrySet())
				{
					props.setProperty(myData.getKey()+ ".OWNER", myData.getValue());
				}
				try (FileOutputStream os = new FileOutputStream(new File(MBAUtils.M_PROPS, "auth.properties")))
				{
					props.storeToXML(os, "authorization properties");
				}
				long finish = System.currentTimeMillis();
				//theServlet.log("updateAuthorizationData completed in " + (finish-start)/1000 + " seconds");
			}
			else
			{
				//theServlet.log("updateAuthorizationData disabled/skipped");
			}
		}
	}
	
	static private void processProp(HttpServlet theServlet, String theProp, String theValue) throws IOException
	{
		//theServlet.log("processProp theProp=" + theProp);
		//theServlet.log("processProp theValue=" + theValue);
		String jobId = theProp.substring(0, theProp.length()-6);
		if (theProp.endsWith(".USERS"))
		{
			mJobsToUsers.put(jobId, stringToTreeSet(theValue));
		}
		else if (theProp.endsWith(".ROLES"))
		{
			mJobsToRoles.put(jobId, stringToTreeSet(theValue));
		}
		else if (theProp.endsWith(".OWNER"))
		{
			mJobsToOwner.put(jobId, theValue);
		}
	}
	
	synchronized static public void readAuthorizationData(HttpServlet theServlet) throws FileNotFoundException, IOException
	{
		if (MBAproperties.isLoginAllowed(theServlet))
		{
			//theServlet.log("readAuthorizationData version = " + edu.mda.bioinfo.mba.servlets.MBAUtils.M_VERSION);
			long start = System.currentTimeMillis();
			PropertiesEsc props = new PropertiesEsc();
			//authorization properties
			try (FileInputStream is = new FileInputStream(new File(MBAUtils.M_PROPS, "auth.properties")))
			{
				props.loadFromXML(is);
			}
			mJobsToUsers.clear();
			mJobsToRoles.clear();
			mJobsToOwner.clear();

			for (Entry<String, String> myData : props.entrySet())
			{
				processProp(theServlet, (String)myData.getKey(), (String)myData.getValue());
			}
			long finish = System.currentTimeMillis();
			//theServlet.log("readAuthorizationData completed in " + (finish-start)/1000 + " seconds");
		}
		else
		{
			//theServlet.log("readAuthorizationData disabled/skipped");
		}
		mIsRead = true;
	}
		
	synchronized static public void userHasAccessException(HttpServlet theServlet, HttpServletRequest theRequest, String theJobId) throws IOException, Exception
	{
		//theServlet.log("userHasAccessException version = " + edu.mda.bioinfo.mba.servlets.MBAUtils.M_VERSION);
		if (false==mIsRead)
		{
			readAuthorizationData(theServlet);
		}
		if (false==userHasAccess(theServlet, theRequest, theJobId))
		{
			throw new Exception("User does not have access");
		}
	}
		
	synchronized static public boolean userHasAccess(HttpServlet theServlet, HttpServletRequest theRequest, String theJobId) throws IOException
	{
		if (false==mIsRead)
		{
			readAuthorizationData(theServlet);
		}
		String username = AuthUpdate.getUserName(theRequest);
		TreeSet<String> roles = AuthUpdate.getUserRoles(theRequest);
		return userHasAccess(theServlet, theJobId, username, roles);
	}
	
	synchronized static public boolean userHasAccess(HttpServlet theServlet, String theJobId, String theUser, TreeSet<String> theUserRoles) throws IOException
	{
		//theServlet.log("userHasAccess version = " + edu.mda.bioinfo.mba.servlets.MBAUtils.M_VERSION);
		boolean grant = false;
		if (MBAproperties.isLoginAllowed(theServlet))
		{
			if (false==mIsRead)
			{
				readAuthorizationData(theServlet);
			}
			//theServlet.log("userHasAccess theJobId=" +theJobId);
			//theServlet.log("userHasAccess theUser=" +theUser);
			// owners always have access
			String owner =  mJobsToOwner.get(theJobId);
			TreeSet<String> users = getJobUsers(theServlet, theJobId);
			TreeSet<String> roles = getJobRoles(theServlet, theJobId);
			//theServlet.log("userHasAccess owner=" +owner);
			//theServlet.log("userHasAccess users=" +users);
			//theServlet.log("userHasAccess roles=" +roles);
			if (null==owner)
			{
				//theServlet.log("userHasAccess null owner, allow");
				// no owner, and/or created before authorization was added
				grant = true;
			}
			else if ("".equals(owner))
			{
				//theServlet.log("userHasAccess empty string owner, allow");
				// no owner, anyone can see
				grant = true;
			}
			else if (owner.equals(theUser))
			{
				//theServlet.log("userHasAccess is owner, allow");
				// owners always have access
				grant = true;
			}
			else if ((null!=theUser)&&(users.contains(theUser)))
			{
				//theServlet.log("userHasAccess in users");
				// if member of users list, allow
				grant = true;
			}
			else if (true==roles.removeAll(theUserRoles))
			{
				//theServlet.log("userHasAccess has role, allow");
				// if roles overlap, which means roles was changed (making removeAll return a true)
				// grant access
				grant = true;
			}
			else
			{
				//theServlet.log("userHasAccess no access");
			}
		}
		else
		{
			grant = true;
			//theServlet.log("userHasAccess disabled/skipped, allow anyone to access");
		}
		return grant;
	}
	
	
}
