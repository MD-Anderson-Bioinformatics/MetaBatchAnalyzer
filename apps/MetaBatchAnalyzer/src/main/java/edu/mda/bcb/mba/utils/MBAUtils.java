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

package edu.mda.bcb.mba.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.Part;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author Tod-Casasent
 */
public class MBAUtils
{
	final public static String M_OUTPUT = "/MBA/OUTPUT";
	final public static String M_PROPS = "/MBA/PROPS";
	final public static String M_UTILS = "/MBA/UTILS";
	final public static String M_WEBSITE = "/MBA/WEBSITE";
	public static final String M_VERSION = "BEA_VERSION_TIMESTAMP";
	
	static public void copyMutationsIfFileExists(File theSource1, File theSource2, File theDataDir, File theMafDir, String theDestName1, String theDestName2) throws IOException
	{
		if (theSource1.exists())
		{
			List<String> dataset = Files.readAllLines(new File(theDataDir, "dataset.txt").toPath());
			String disease = dataset.get(1);
			String platform = dataset.get(3);
			platform = platform.replaceAll(" ", "");
			File destDir = new File( new File( new File( new File( new File( new File(theMafDir, disease),  "data5"), platform),  "data3"),  "data2"), "data1");
			File destFile1 = new File( destDir, theDestName1);
			File destFile2 = new File( destDir, theDestName2);
			destDir.mkdirs();
			Files.copy(theSource1.toPath(), destFile1.toPath());
			Files.copy(theSource2.toPath(), destFile2.toPath());
		}
	}
	
	static public String uploadTextOnlyFile(Part thePart, String theSavePath, HttpServlet theServlet, String theSuccessString) throws FileNotFoundException, IOException, SAXException, TikaException
	{
		String message = "";
		String tmpPath = theSavePath + ".tmp";
		// upload and check file for text only
		try(OutputStream out = new FileOutputStream(tmpPath))
		{
			BodyContentHandler handler = new BodyContentHandler(out);
			TXTParser parser = new TXTParser();
			Metadata metadata = new Metadata();
			ParseContext context = new ParseContext();
			try (TikaInputStream stream = TikaInputStream.get(thePart.getInputStream()))
			{
				parser.parse(stream, handler, metadata, context);
				theServlet.log("File mbang uploaded to " + tmpPath);
			}
		}
		String filetype = new Tika().detect(tmpPath);
		theServlet.log("filetype = " + filetype);
		// remove empty lines inserted by text check
		try (BufferedReader br = new BufferedReader(new FileReader(tmpPath)))
		{
			boolean first = true;
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(theSavePath)))
			{
				theServlet.log("File mbang cleaned to " + theSavePath);
				String line;
				while ((line = br.readLine()) != null)
				{
					if (first)
					{
						bw.write(line);
						first = false;
					}
					else
					{
						if (!"".equals(line))
						{
							bw.newLine();
							bw.write(line);
						}
					}
				}
				FileUtils.delete(new File(tmpPath));
				message = theSuccessString;
			}
		}
		return message;
	}
}
