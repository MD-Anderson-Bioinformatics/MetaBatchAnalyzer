/*
 *  Copyright (c) 2011-2022 University of Texas MD Anderson Cancer Center
 *  
 *  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
 *  MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

 */
package edu.mda.bcb.mba.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.text.StringEscapeUtils;

/**
 *
 * @author Tod_Casasent
 */
public class PropertiesEsc
{
	private Properties mProperties = null;
	
	public PropertiesEsc()
	{
		mProperties = new Properties();
	}
	
	public void loadFromXML(FileInputStream theInput) throws IOException
	{
		mProperties.loadFromXML(theInput);
		// check if properties are escaped
		if (null!=mProperties.getProperty("bcb-escape-esc"))
		{
			// if not already escaped, set to escaped
			for (Entry<Object, Object> entry : mProperties.entrySet())
			{
				String val = (String)(entry.getValue());
				val = StringEscapeUtils.escapeXml11(val);
				mProperties.setProperty((String)(entry.getKey()), val);
			}
			mProperties.setProperty("bcb-escape-esc", "bcb-escape-esc");
		}
	}
	
	public void storeToXML(OutputStream theOutput, String theComment) throws IOException
	{
		mProperties.storeToXML(theOutput, theComment);
	}
	
	public String setProperty(String theKey, String theValue)
	{
		String old = (String)(mProperties.setProperty(theKey, StringEscapeUtils.escapeXml11(theValue)));
		return StringEscapeUtils.unescapeXml(old);
	}
	
	public Set<Entry<String, String>> entrySet()
	{
		HashMap<String, String> unescSet = new HashMap<>();
		for (Entry<Object, Object> entry : mProperties.entrySet())
		{
			unescSet.put((String)(entry.getKey()), StringEscapeUtils.unescapeXml((String)(entry.getValue())));
		}
		return unescSet.entrySet();
	}
	
	public Set<String> stringPropertyNames()
	{
		return mProperties.stringPropertyNames();
	}
	
	public String getProperty(String theKey)
	{
		return StringEscapeUtils.unescapeXml((String)(mProperties.getProperty(theKey)));
	}
	
	public String getProperty(String theKey, String theDefaultValue)
	{
		return StringEscapeUtils.unescapeXml((String)(mProperties.getProperty(theKey, StringEscapeUtils.escapeXml11(theDefaultValue))));
	}
	
	public String remove(String theKey)
	{
		return (String)(mProperties.remove(theKey));
	}
	
	public int size()
	{
		return mProperties.size();
	}
	
	public Set<Object> keySet()
	{
		return mProperties.keySet();
	}
}
