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

package edu.mda.bcb.mba.authorization;

import edu.mda.bcb.mba.utils.PropertiesEsc;
import java.util.HashMap;
import java.util.TreeSet;

/**
 *
 * @author Tod-Casasent
 */
public class UserAndRoleData
{
	static private HashMap<String, TreeSet<String>> mUsersToRoles =  new HashMap<>();
	static private HashMap<String, TreeSet<String>> mRolesToUsers =  new HashMap<>();
	
	private UserAndRoleData()
	{
		
	}
	
	synchronized static public void updateUserAndRoleData(PropertiesEsc theUsers)
	{
		mUsersToRoles.clear();
		mRolesToUsers.clear();
		for( String userId : theUsers.stringPropertyNames())
		{
			TreeSet<String> roles = mUsersToRoles.get(userId);
			if (null==roles)
			{
				roles = new TreeSet<>();
			}
			for (String role : theUsers.getProperty(userId).split("\\|", -1))
			{
				roles.add(role);
				TreeSet<String> users = mRolesToUsers.get(role);
				if (null==users)
				{
					users = new TreeSet<>();
				}
				users.add(userId);
				mRolesToUsers.put(role, users);
			}
			mUsersToRoles.put(userId, roles);
		}
	}
	
	synchronized static public TreeSet<String> getUserList()
	{
		TreeSet<String> set = new TreeSet<>();
		set.addAll(mUsersToRoles.keySet());
		return set;
	}
	
	synchronized static public TreeSet<String> getRoleList()
	{
		TreeSet<String> set = new TreeSet<>();
		set.addAll(mRolesToUsers.keySet());
		return set;
	}
	
	synchronized static public TreeSet<String> getUsersInRoleList(String theRole)
	{
		TreeSet<String> set = new TreeSet<>();
		set.addAll(mRolesToUsers.get(theRole));
		return set;
	}
}
