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
import edu.mda.bcb.mba.utils.PropertiesEsc;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "MBAproperties", urlPatterns =
{
	"/MBAproperties"
}, loadOnStartup=1)
public class MBAproperties extends MBAServletMixin
{
	static private String M_PROPERTIES_JSON = null;
	static private PropertiesEsc M_PROPERTIES = null;
	static private long M_TIMESTAMP = 0;
	
	static public String getProperty(String theProperty, HttpServlet theServlet) throws IOException
	{
		if (null==M_PROPERTIES)
		{
			getResponseString(theServlet);
		}
		return M_PROPERTIES.getProperty(theProperty);
	}
	
	static public boolean isLoginAllowed(HttpServlet theServlet) throws IOException
	{
		if (null==M_PROPERTIES)
		{
			getResponseString(theServlet);
		}
		return "true".equals(M_PROPERTIES.getProperty("allowLogin"));
	}

	public MBAproperties()
	{
		super("application/json;charset=UTF-8", true, null);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		// return to user handled in parent
		//long start = System.currentTimeMillis();
		theBuffer.append(getResponseString(this));
		//long finish = System.currentTimeMillis();
		//log("MBAproperties completed in " + (finish-start)/1000 + " seconds");
	}

	synchronized public static String getResponseString(HttpServlet theServlet) throws IOException
	{
		//theServlet.log("MBAproperties::getResponseString");
		//theServlet.log("MBAproperties::getResponseString M_TIMESTAMP="+M_TIMESTAMP);
		//theServlet.log("MBAproperties::getResponseString M_TIMESTAMP="+System.currentTimeMillis());
		// TODO: for the timeout case, put that into a separate thread, perha[s using a listener instead of servlet load
		String result = M_PROPERTIES_JSON;
		if ((null==M_PROPERTIES_JSON)||
			((null!=M_PROPERTIES_JSON)&&((M_TIMESTAMP-System.currentTimeMillis())>(1000*60*60))))
		{
			M_PROPERTIES = new PropertiesEsc();
			try (FileInputStream is = new FileInputStream(new File(MBAUtils.M_PROPS, "mba.properties")))
			{
				M_PROPERTIES.loadFromXML(is);
			}
			StringWriter out = new StringWriter();
			out.append("{");
			boolean wrote = false;
			for (String name : M_PROPERTIES.stringPropertyNames())
			{
				if (true==wrote)
				{
					out.append(",");
				}
				out.append("\n\"" + name + "\":\"" + M_PROPERTIES.getProperty(name) + "\"");
				theServlet.log("MBAproperties " + name + "=" + M_PROPERTIES.getProperty(name));
				wrote = true;
			}
			out.append("\n}\n");
			M_PROPERTIES_JSON = out.toString();
			M_TIMESTAMP = System.currentTimeMillis();
			result = M_PROPERTIES_JSON;
		}
		return result;
	}
}
