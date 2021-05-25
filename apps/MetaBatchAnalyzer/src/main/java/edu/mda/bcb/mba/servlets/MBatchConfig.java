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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "MBatchConfig", urlPatterns =
{
	"/MBatchConfig"
})
public class MBatchConfig extends MBAServletMixin
{
	public MBatchConfig()
	{
		super("application/json;charset=UTF-8", true, MBatchConfig.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		log("passed in jobId is " + jobId);
		String action = request.getParameter("action");
		log("passed in action is " + action);
		// action options
		boolean readOrInitFile = ("initialize".equals(action));
		boolean writeFile = ("write".equals(action));
		// configDesc	MutBatch
		boolean isMutBatch = false;
		String configDesc = request.getParameter("configDesc");
		if ("MutBatch".equals(configDesc))
		{
			isMutBatch = true;
		}
		// get list of parameters and write them
		File jobDir = new File(MBAUtils.M_OUTPUT, jobId);
		Enumeration<String> sEnum = request.getParameterNames();
		while(sEnum.hasMoreElements())
		{
			String paraName = sEnum.nextElement();
			String paraValue = request.getParameter(paraName);
			log("'" + paraName + "' is '" + paraValue + "'");
		}
		File zipResultsDir = new File(jobDir, "ZIP-RESULTS");
		zipResultsDir.mkdirs();
		File configFile = new File(zipResultsDir, "MBatchConfig.tsv");
		if (true==readOrInitFile)
		{
			if (!configFile.exists())
			{
				writeConfigFile(request, configFile, isMutBatch, jobDir, jobId);
			}
		}
		else if (true==writeFile)
		{
			writeConfigFile(request, configFile, isMutBatch, jobDir, jobId);
		}
		readConfigFile(theBuffer, configFile);
	}
	
	public void writeConfigFile(HttpServletRequest theRequest, File theConfigFile, boolean theMutBatchFlag, File theJobDir, String theJobId) throws IOException
	{
		OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
		try(BufferedWriter bw = java.nio.file.Files.newBufferedWriter(Paths.get(theConfigFile.getAbsolutePath()), Charset.availableCharsets().get("UTF-8"), options))
		{
			boolean foundTitle = false;
			Enumeration<String> sEnum = theRequest.getParameterNames();
			while(sEnum.hasMoreElements())
			{
				String paraName = sEnum.nextElement();
				String value = null;
				if ((!"_".equals(paraName))&&(!"action".equals(paraName)))
				{
					if ("title".equals(paraName))
					{
						foundTitle = true;
					}
					if (paraName.endsWith("[]"))
					{
						//"batchTypesForMBatch[]"
						String [] tmp = theRequest.getParameterValues(paraName);
						paraName = paraName.replace("[]", "");
						if (null!=tmp)
						{
							for (String val : tmp)
							{
								if (null==value)
								{
									value = val;
								}
								else
								{
									value = value + "," + val;
								}
							}
						}
					}
					else if (paraName.endsWith("Flag"))
					{
						value = theRequest.getParameter(paraName);
						if ("true".equalsIgnoreCase(value))
						{
							value = "TRUE";
						}
						else
						{
							value = "FALSE";
						}
					}
					else
					{
						value = theRequest.getParameter(paraName);
					}
					bw.write(paraName + "\t" + value);
					bw.newLine();
					if ("batchTypesForMBatchArray".equals(paraName))
					{
						bw.write("batchTypesForTRINOVA" + "\t" + value);
						bw.newLine();
					}
				}
			}
			if (false==foundTitle)
			{
				bw.write("title\tBatch Effects Run from MBA");
				bw.newLine();
			}
			File mafFiles = new File(new File(new File(theJobDir, "ZIP-DATA"), "original"), "MUT_MAFS");
			File mutFiles = new File(new File(new File(theJobDir, "ZIP-DATA"), "original"), "MUT_EXTRACT");
			File zipResults = new File(new File(theJobDir, "ZIP-RESULTS"), "MutBatch");
			if (mafFiles.exists())
			{
				FileUtils.deleteDirectory(mafFiles);
			}
			if (true==theMutBatchFlag)
			{
				bw.write("mutBatchDataBaseDir\t" + mafFiles.getAbsolutePath());
				bw.newLine();
				bw.write("mutBatchExtractDir\t" + mutFiles.getAbsolutePath());
				bw.newLine();
				bw.write("mutBatchOutputDir\t" + zipResults.getAbsolutePath());
				bw.newLine();
				mafFiles.mkdirs();
				// copyMutationsIfFileExists(File theSource, File theDataDir, File theMafDir, String theDestName) 
				MBAUtils.copyMutationsIfFileExists(new File(theJobDir, "mutations.tsv"), new File(theJobDir, "batches.tsv"), new File(theJobDir, "PRI"), mafFiles, "mutations.tsv", "batches.tsv");
				MBAUtils.copyMutationsIfFileExists(new File(theJobDir, "mutations2.tsv"), new File(theJobDir, "batches2.tsv"), new File(theJobDir, "SEC"), mafFiles, "mutations.tsv", "batches.tsv");
			}
		}
	}

	public void readConfigFile(StringBuffer theBuffer, File theConfigFile) throws IOException
	{
		List<String> lines = java.nio.file.Files.readAllLines(theConfigFile.toPath());
		boolean wrote = false;
		theBuffer.append("{");
		for (String line : lines)
		{
			String [] splitted = line.split("\t");
			if (wrote)
			{
				theBuffer.append(",");
			}
			else
			{
				wrote = true;
			}
			if (splitted[0].endsWith("Array"))
			{
				if ("null".equals(splitted[1]))
				{
					theBuffer.append("\n\"" + StringEscapeUtils.escapeJson(splitted[0]) + "\":[]");
				}
				else
				{
					theBuffer.append("\n\"" + StringEscapeUtils.escapeJson(splitted[0]) + "\":[");
					String [] arrayList = splitted[1].split(",", -1);
					boolean listStarted = false;
					for (String ele : arrayList)
					{
						if (true==listStarted)
						{
							theBuffer.append(",");
						}
						else
						{
							listStarted = true;
						}
						theBuffer.append("\"" + StringEscapeUtils.escapeJson(ele) + "\"");
					}
					theBuffer.append("]");
				}
			}
			else if (splitted[0].endsWith("Flag"))
			{
				if ("true".equalsIgnoreCase(splitted[1]))
				{
					theBuffer.append("\n\"" + StringEscapeUtils.escapeJson(splitted[0]) + "\":\"true\"");
				}
				else
				{
					theBuffer.append("\n\"" + StringEscapeUtils.escapeJson(splitted[0]) + "\":\"false\"");
				}
			}
			else
			{
				theBuffer.append("\n\"" + StringEscapeUtils.escapeJson(splitted[0]) + "\":\"" + StringEscapeUtils.escapeJson(splitted[1]) + "\"");
			}
		}
		theBuffer.append("\n}\n");
	}
}
