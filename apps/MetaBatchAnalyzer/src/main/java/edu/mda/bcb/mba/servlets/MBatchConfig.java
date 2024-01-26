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
package edu.mda.bcb.mba.servlets;

import edu.mda.bcb.mba.status.JobStatus;
import edu.mda.bcb.mba.utils.MBAUtils;
import edu.mda.bcb.mba.utils.ScanCheck;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
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

	protected void checkMbatchConfigData(HttpServletRequest theRequest) throws Exception
	{
		//
		String action = theRequest.getParameter("action");
		if ((!"initialize".equals(action)) && (!"write".equals(action)))
		{
			throw new Exception("Illegal Action value");
		}
		//
		String configDesc = theRequest.getParameter("configDesc");
		if ((!"MBatch".equals(configDesc)) && (!"MutBatch".equals(configDesc)))
		{
			throw new Exception("Illegal configDesc value");
		}
		//
		Enumeration<String> sEnum = theRequest.getParameterNames();
		while (sEnum.hasMoreElements())
		{
			String paraName = sEnum.nextElement();
			String paraValue = theRequest.getParameter(paraName);
			if (("jobId".equals(paraName)) || ("configDesc".equals(paraName))
					|| ("Title".equals(paraName))
					|| ("DataVersion".equals(paraName)) || ("TestVersion".equals(paraName))
					|| ("mutBatchMem".equals(paraName)) || ("RBN_InvariantId".equals(paraName))
					|| ("RBN_VariantId".equals(paraName)) || ("EBNPlus_GroupId1".equals(paraName))
					|| ("EBNPlus_GroupId2".equals(paraName)) || ("RBN_InvariantRepsType".equals(paraName))
					|| ("RBN_VariantRepsType".equals(paraName)) || ("sampleidBatchType".equals(paraName))
					|| ("filteringBatchType".equals(paraName)) || ("ngchmRowType".equals(paraName))
					|| ("ngchmColumnType".equals(paraName)) || ("title".equals(paraName)))
			{
				ScanCheck.checkForMetaCharacters(paraValue);
			}
			else
			{
				if (("mutationsMutbatchFlag".equals(paraName))
						|| ("filterLogTransformFlag".equals(paraName)) || ("CDP_Flag".equals(paraName))
						|| ("selectedNgchmFlag".equals(paraName)) || ("filterLogTransformFlag2".equals(paraName))
						|| ("RBN_Only".equals(paraName)) || ("RBN_UseFirstAsInvariantFlag".equals(paraName))
						|| ("RBN_Matched".equals(paraName)))
				{
					ScanCheck.checkForMetaCharacters(paraValue);
					ScanCheck.checkForBoolean(paraValue);
				}
				else
				{
					if (("mutBatchThreads".equals(paraName))
							|| ("filterMaxValue".equals(paraName)) || ("selectedCorrectionMinBatchSize".equals(paraName))
							|| ("selectedDSCPermutations".equals(paraName)) || ("selectedDSCThreads".equals(paraName))
							|| ("selectedDSCMinBatchSize".equals(paraName)) || ("selectedDSCSeed".equals(paraName))
							|| ("selectedDSCMaxGeneCount".equals(paraName)) || ("selectedBoxplotMaxGeneCount".equals(paraName))
							|| ("EBNPlus_Seed".equals(paraName)) || ("EBNPlus_MinSamples".equals(paraName)))
					{
						ScanCheck.checkForMetaCharacters(paraValue);
						ScanCheck.checkForInt(paraValue);
					}
					else
					{
						if (("mutBatchPvalueCutoff".equals(paraName))
								|| ("mutBatchZscoreCutoff".equals(paraName)) || ("".equals(paraName))
								|| ("".equals(paraName)) || ("".equals(paraName)))
						{
							ScanCheck.checkForMetaCharacters(paraValue);
							ScanCheck.checkForFloat(paraValue);
						}
						else
						{
							if (("batchTypesForMBatchArray".equals(paraName))
									|| ("filteringBatchesArray".equals(paraName)) || ("RBN_InvariantRepsArray".equals(paraName))
									|| ("RBN_VariantRepsArray".equals(paraName)) || ("".equals(paraName))
									|| ("".equals(paraName)) || ("".equals(paraName))
									|| ("".equals(paraName)) || ("".equals(paraName)))
							{
								// comma delimited list or empty
								ScanCheck.checkForMetaCharacters(paraValue);
								// checkForBatchTypesAndValues(paraValue);
							}
							else
							{
								if (("mutBatchDataBaseDir".equals(paraName))
										|| ("mutBatchExtractDir".equals(paraName)) || ("mutBatchOutputDir".equals(paraName)))
								{
									// legal directory path
									ScanCheck.checkForMetaCharacters(paraValue);
									// checkForBatchTypesAndValues(paraValue);
								}
								else
								{
									if ("selectedCorrection".equals(paraName))
									{
										if ( (!"null".equals(paraValue)) && (!"none".equals(paraValue)) && 
												(!"ANOVA_adj".equals(paraValue))
												&& (!"ANOVA_unadj".equals(paraValue)) && (!"EB_withPara".equals(paraValue))
												&& (!"EB_withNonpara".equals(paraValue)) && (!"MP_batch".equals(paraValue))
												&& (!"MP_overall".equals(paraValue)) && (!"RBN_Replicates".equals(paraValue))
												&& (!"RBN_Pseudoreps".equals(paraValue)) && (!"EBN_Plus".equals(paraValue)))
										{
											throw new Exception("Illegal Correction found");
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		// return to user handled in parent
		checkMbatchConfigData(request);
		String jobId = request.getParameter("jobId");
		log("passed in jobId is " + jobId);
		JobStatus.checkJobId(jobId);
		String action = request.getParameter("action");
		log("passed in action is " + action);
		// action options
		boolean readOrInitFile = ("initialize".equals(action));
		boolean writeFile = ("write".equals(action));
		// configDesc	MutBatch
		boolean isMutationsMutBatch = false;
		String configDesc = request.getParameter("configDesc");
		if ("MutBatch".equals(configDesc))
		{
			isMutationsMutBatch = true;
		}
		// get list of parameters and write them
		File jobDir = new File(MBAUtils.M_OUTPUT, jobId);
		Enumeration<String> sEnum = request.getParameterNames();
		while (sEnum.hasMoreElements())
		{
			String paraName = sEnum.nextElement();
			String paraValue = request.getParameter(paraName);
			log("'" + paraName + "' is '" + paraValue + "'");
		}
		File zipResultsDir = new File(jobDir, "ZIP-RESULTS");
		zipResultsDir.mkdirs();
		File configFile = new File(zipResultsDir, "MBatchConfig.tsv");
		if (true == readOrInitFile)
		{
			if (!configFile.exists())
			{
				writeConfigFile(request, configFile, isMutationsMutBatch, jobDir, jobId);
			}
		}
		else
		{
			if (true == writeFile)
			{
				writeConfigFile(request, configFile, isMutationsMutBatch, jobDir, jobId);
			}
		}
		readConfigFile(theBuffer, configFile);
	}

	public void writeConfigFile(HttpServletRequest theRequest, File theConfigFile,
			boolean theMutationsMutBatchFlag, File theJobDir, String theJobId) throws IOException
	{
		OpenOption[] options = new OpenOption[]
		{
			StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
		};
		try (BufferedWriter bw = java.nio.file.Files.newBufferedWriter(Paths.get(theConfigFile.getAbsolutePath()), Charset.availableCharsets().get("UTF-8"), options))
		{
			boolean foundTitle = false;
			Enumeration<String> sEnum = theRequest.getParameterNames();
			while (sEnum.hasMoreElements())
			{
				String paraName = sEnum.nextElement();
				String value = null;
				if ((!"_".equals(paraName)) && (!"action".equals(paraName)))
				{
					if ("Title".equals(paraName))
					{
						foundTitle = true;
					}
					if (paraName.endsWith("[]"))
					{
						//"batchTypesForMBatch[]"
						String[] tmp = theRequest.getParameterValues(paraName);
						paraName = paraName.replace("[]", "");
						if (null != tmp)
						{
							for (String val : tmp)
							{
								if (null == value)
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
					else
					{
						if (paraName.endsWith("Flag"))
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
					}
					bw.write(paraName + "\t" + value);
					bw.newLine();
				}
			}
			if (false == foundTitle)
			{
				bw.write("Title\tBatch Effects Run from MBA");
				bw.newLine();
			}
			File mafFiles = new File(new File(new File(theJobDir, "ZIP-DATA"), "original"), "MUT_MAFS");
			File mutFiles = new File(new File(new File(theJobDir, "ZIP-DATA"), "original"), "MUT_EXTRACT");
			File zipResults = new File(new File(theJobDir, "ZIP-RESULTS"), "MutBatch");
			if (mafFiles.exists())
			{
				FileUtils.deleteDirectory(mafFiles);
			}
			if (true == theMutationsMutBatchFlag)
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
			String[] splitted = line.split("\t");
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
					String[] arrayList = splitted[1].split(",", -1);
					boolean listStarted = false;
					for (String ele : arrayList)
					{
						if (true == listStarted)
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
			else
			{
				if (splitted[0].endsWith("Flag"))
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
		}
		theBuffer.append("\n}\n");
	}
}
