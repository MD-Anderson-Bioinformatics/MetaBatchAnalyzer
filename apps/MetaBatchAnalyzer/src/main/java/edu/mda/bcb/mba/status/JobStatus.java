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
package edu.mda.bcb.mba.status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.mda.bcb.mba.authorization.Authorization;
import edu.mda.bcb.mba.utils.MBAUtils;
import edu.mda.bcb.mba.servlets.MBAproperties;
import edu.mda.bcb.mba.servlets.UploadBatch;
import edu.mda.bcb.mba.utils.PropertiesEsc;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.TreeSet;
import jakarta.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServlet;
import org.apache.commons.text.StringEscapeUtils;

/**
 *
 * @author Tod-Casasent
 */
public class JobStatus
{

	static private PropertiesEsc M_JOB_STATUS = null;

	static public void readProperties() throws FileNotFoundException, IOException
	{
		// TODO: this reads the job file every time--change to only load when needed if this is too slow
		//if (null==M_JOB_STATUS)
		{
			M_JOB_STATUS = new PropertiesEsc();
			File myfile = new File(MBAUtils.M_PROPS, "job.properties");
			if (myfile.exists())
			{
				try (FileInputStream is = new FileInputStream(myfile))
				{
					M_JOB_STATUS.loadFromXML(is);
				}
			}
		}
	}

	static public void writeProperties() throws FileNotFoundException, IOException
	{
		if (null != M_JOB_STATUS)
		{
			try (FileOutputStream os = new FileOutputStream(new File(MBAUtils.M_PROPS, "job.properties")))
			{
				M_JOB_STATUS.storeToXML(os, "job status");
			}
		}
	}

	synchronized static public void buildJobStatusList(HttpServlet theServlet, StringBuffer theBuffer, String theUsername, TreeSet<String> theRoles) throws IOException
	{
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		boolean wrote = false;
		readProperties();
		if (M_JOB_STATUS.size() > 0)
		{
			TreeSet<String> sortedList = new TreeSet<>();
			for (Object foo : M_JOB_STATUS.keySet())
			{
				// this ensures job info entries don't get returned in list
				if (!foo.toString().contains("."))
				{
					sortedList.add(foo.toString());
				}
			}
			for (String jobid : sortedList)
			{
				if (Authorization.userHasAccess(theServlet, jobid, theUsername, theRoles))
				{
					if (false == wrote)
					{
						wrote = true;
					}
					else
					{
						theBuffer.append(",");
					}
					JOB_STATUS status = JOB_STATUS.StringToEnum(M_JOB_STATUS.getProperty(jobid));
					theBuffer.append("{");
					theBuffer.append("\n\"jobid\":\"" + jobid + "\",");
					theBuffer.append("\n\"status\":\"" + status.mStatus + "\",");
					theBuffer.append("\n\"message\":\"" + status.mReport + "\",");
					theBuffer.append("\n\"tag\":\"" + StringEscapeUtils.escapeJson(JobStatus.M_JOB_STATUS.getProperty(jobid + ".tag", "")) + "\",");
					theBuffer.append("\n\"owner\":\"" + StringEscapeUtils.escapeJson(JobStatus.M_JOB_STATUS.getProperty(jobid + ".owner", "")) + "\",");
					theBuffer.append("\n\"email\":\"" + StringEscapeUtils.escapeJson(JobStatus.M_JOB_STATUS.getProperty(jobid + ".email", "")) + "\",");
					theBuffer.append("\n\"authRoles\":" + gson.toJson(Authorization.getJobRoles(theServlet, jobid)) + ",");
					theBuffer.append("\n\"authUsers\":" + gson.toJson(Authorization.getJobUsers(theServlet, jobid)));
					theBuffer.append("\n}\n");
				}
			}
		}
	}

	synchronized static public void setJobStatus(String theJob, JOB_STATUS theStatus, HttpServletRequest theRequest, HttpServlet theServlet)
			throws FileNotFoundException, IOException, Exception
	{
		if (JOB_STATUS.MBATCHRUN_END_SUCCESS == theStatus)
		{
			copyToWebsite(theJob, theRequest, theServlet);
		}
		readProperties();
		JOB_STATUS status1 = getJobStatus(theJob);
		if ((status1 == JOB_STATUS.NEWJOB_PRIMARY_MW_WAIT) && (theStatus == JOB_STATUS.NEWJOB_PRIMARY_DONE))
		{
			File jobDir = new File(MBAUtils.M_OUTPUT, theJob);
			File zipDataDir = new File(jobDir, "ZIP-DATA");
			File zipDataOriginalDir = new File(zipDataDir, "original");
			final String savePath = new File(zipDataOriginalDir, "batches.tsv").getAbsolutePath();
			UploadBatch.batchesPostProcessing(savePath, false, false, false, true, true, false, true);
		}
		M_JOB_STATUS.setProperty(theJob, theStatus.mStatus);
		writeProperties();
		boolean sendEmail = false;
		JOB_STATUS status2 = getJobStatus(theJob);
		if (status1.mStatus.endsWith("_WAIT"))
		{
			if (status1 != status2)
			{
				sendEmail = true;
			}
		}
		if (true == sendEmail)
		{
			try
			{
				SendEmail.sendEmail(theJob, theServlet);
			}
			catch (MessagingException exp)
			{
				theServlet.log("Error sending email for " + theJob, exp);
			}
		}
	}

	synchronized static public void setJobInfo(String theJob, String theTag, String theEmail) throws FileNotFoundException, IOException
	{
		readProperties();
		if (null == theTag)
		{
			theTag = "";
		}
		if (null == theEmail)
		{
			theEmail = "";
		}
		// TODO: Possibly expand these if statments to check if key exits and value@key isn't null
		M_JOB_STATUS.setProperty(theJob + ".tag", theTag);
		//TODO: Add owner to job info M_JOB_STATUS.setProperty(theJob + ".owner", theJobInfo.get("owner"));
		M_JOB_STATUS.setProperty(theJob + ".email", theEmail);
		writeProperties();
		// jobStatusPostProcessing? I think no, because updating the info shouldn't (and can't) change the job status
	}

	synchronized static public void createNewJob(String theJob, String theUser, HttpServlet theServlet) throws FileNotFoundException, IOException
	{
		// createNewJob is defined independantly rather than executiing setJobStatus and setJobInfo
		// back to back to avoid reading and writing twice unncessisarily.
		readProperties();
		M_JOB_STATUS.setProperty(theJob, JOB_STATUS.NEWJOB_START.mStatus);
		M_JOB_STATUS.setProperty(theJob + ".tag", "");
		M_JOB_STATUS.setProperty(theJob + ".owner", (null == theUser ? "" : theUser));
		M_JOB_STATUS.setProperty(theJob + ".email", "");
		writeProperties();
		if ((!"".equals(theUser)) && (null != theUser))
		{
			Authorization.updateAuthorizationData(theServlet, theJob, theUser, new TreeSet<String>(), new TreeSet<String>());
		}
	}

	synchronized static public void deleteJob(String theJob, HttpServlet theServlet) throws FileNotFoundException, IOException
	{
		readProperties();
		for (String prop : M_JOB_STATUS.stringPropertyNames())
		{
			if (prop.startsWith(theJob))
			{
				M_JOB_STATUS.remove(prop);
			}
		}
		writeProperties();
		Authorization.removeAuthorizationData(theServlet, theJob);
	}

	synchronized static public JOB_STATUS getJobStatus(String theJob) throws FileNotFoundException, IOException
	{
		readProperties();
		return (JOB_STATUS.StringToEnum(M_JOB_STATUS.getProperty(theJob)));
	}

	static public void checkJobId(String theJobId) throws Exception
	{
		ArrayList<String> jobs = new ArrayList<>(Arrays.asList(JobStatus.getJobList()));
		if (!jobs.contains(theJobId))
		{
			throw new Exception("Job not found");
		}
	}

	synchronized static public String[] getJobList() throws IOException
	{
		// NOTE: This function only returns the JobId, Status entries of job.properties.
		// This is done so the JSON created in JOBlist.processRequest is properly formed
		readProperties();
		TreeSet<String> trees = new TreeSet<>();
		if (M_JOB_STATUS.size() > 0)
		{
			for (Object foo : M_JOB_STATUS.keySet())
			{
				// this ensures job info entries don't get returned in list
				if (!foo.toString().contains("."))
				{
					trees.add((String) foo);
				}

			}
		}
		return trees.toArray(new String[0]);
	}

	synchronized static public String getWithJobStatusUpdate(JOB_STATUS theOldStatusA, JOB_STATUS theNewStatusA, JOB_STATUS theOldStatusB, JOB_STATUS theNewStatusB,
			HttpServletRequest theRequest, HttpServlet theServlet) throws IOException, Exception
	{

		String jobId = null;
		readProperties();
		if (M_JOB_STATUS.size() > 0)
		{
			// sort to get oldest first
			TreeSet<String> keys = new TreeSet<>();
			for (Object strObj : M_JOB_STATUS.keySet())
			{
				if (null == jobId)
				{
					if (theOldStatusA.mStatus.equals(M_JOB_STATUS.getProperty((String) strObj)))
					{
						jobId = (String) strObj;
						setJobStatus(jobId, theNewStatusA, theRequest, theServlet);
					}
					else
					{
						if (null != theOldStatusB)
						{
							if (theOldStatusB.mStatus.equals(M_JOB_STATUS.getProperty((String) strObj)))
							{
								jobId = (String) strObj;
								setJobStatus(jobId, theNewStatusB, theRequest, theServlet);
							}
						}
					}
				}
			}
		}
		if (null == jobId)
		{
			jobId = "none";
		}
		return jobId;
	}

	static public HashMap<String, String> getJobMap(String theJob) throws IOException
	{
		HashMap<String, String> map = new HashMap<>();
		JOB_STATUS status = JobStatus.getJobStatus(theJob);
		map.put("jobid", theJob);
		map.put("status", status.mStatus);
		map.put("message", status.mReport);
		map.put("tag", JobStatus.M_JOB_STATUS.getProperty(theJob + ".tag", ""));
		map.put("owner", JobStatus.M_JOB_STATUS.getProperty(theJob + ".owner", ""));
		map.put("email", JobStatus.M_JOB_STATUS.getProperty(theJob + ".email", ""));
		String[] tail = getTailForStatus(theJob, status);
		String myTail = "";
		for (String line : tail)
		{
			myTail = myTail + line + "\n\n";
		}
		map.put("tail", myTail);
		return map;
	}

	// isLoginAllowed()
	static public String getResponseString(HttpServlet theServlet, String theJob, boolean includeTail) throws IOException
	{
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		//
		JOB_STATUS status = JobStatus.getJobStatus(theJob);
		StringWriter out = new StringWriter();
		out.append("{");
		out.append("\n\"jobid\":\"" + theJob + "\",");
		out.append("\n\"status\":\"" + status.mStatus + "\",");
		out.append("\n\"message\":\"" + status.mReport + "\",");
		out.append("\n\"tag\":\"" + StringEscapeUtils.escapeJson(JobStatus.M_JOB_STATUS.getProperty(theJob + ".tag", "")) + "\",");
		out.append("\n\"owner\":\"" + StringEscapeUtils.escapeJson(JobStatus.M_JOB_STATUS.getProperty(theJob + ".owner", "")) + "\",");
		out.append("\n\"email\":\"" + StringEscapeUtils.escapeJson(JobStatus.M_JOB_STATUS.getProperty(theJob + ".email", "")) + "\",");
		out.append("\n\"authRoles\":" + gson.toJson(Authorization.getJobRoles(theServlet, theJob)) + ",");
		out.append("\n\"authUsers\":" + gson.toJson(Authorization.getJobUsers(theServlet, theJob)));
		if (includeTail)
		{
			out.append(",\n\"tail\":[");
			String[] tail = getTailForStatus(theJob, status);
			if ((null != tail) && (tail.length > 0))
			{
				boolean wrote = false;
				for (String line : tail)
				{
					if (true == wrote)
					{
						out.append(",");
					}
					else
					{
						wrote = true;
					}
					out.append("\n\"" + line + "\"");
				}
			}
			out.append("]");
		}
		out.append("\n}\n");
		String json = out.toString();
		return json;
	}

	static public String[] getTailForStatus(String theJob, JOB_STATUS theStatus)
	{
		String[] results = new String[1];
		File tailMe = null;
		if (theStatus.mStatus.startsWith("NEWJOB_"))
		{
			tailMe = new File(new File(MBAUtils.M_OUTPUT, theJob), "DatasetConfig2.log");
			if (!tailMe.exists())
			{
				tailMe = new File(new File(MBAUtils.M_OUTPUT, theJob), "DatasetConfig.log");
				if (!tailMe.exists())
				{
					tailMe = null;
				}
			}
		}
		else
		{
			if (theStatus.mStatus.startsWith("MBATCHCONFIG_"))
			{
				tailMe = new File(new File(MBAUtils.M_OUTPUT, theJob), "MBatchConfig_err.log");
				if (!tailMe.exists())
				{
					tailMe = null;
				}
			}
			else
			{
				if (theStatus.mStatus.startsWith("MBATCHRUN_"))
				{
					tailMe = new File(new File(MBAUtils.M_OUTPUT, theJob), "log.rLog");
					if (!tailMe.exists())
					{
						tailMe = null;
					}
				}
			}
		}
		// add finding log based on data mbang used
		if (null != tailMe)
		{
			try
			{
				ArrayList<String> moreLogs = new ArrayList<>();
				moreLogs.add("");
				moreLogs.add("");
				moreLogs.add(tailMe.getAbsolutePath());
				moreLogs.addAll(Arrays.asList(FileTail.tail(tailMe.getAbsolutePath(), 100)));
				File[] logs = new File(MBAUtils.M_OUTPUT, theJob).listFiles(new FilenameFilter()
				{
					@Override
					public boolean accept(File dir, String name)
					{
						return name.endsWith("_error.log");
					}
				});
				for (File myLog : logs)
				{
					moreLogs.add("");
					moreLogs.add("");
					moreLogs.add(tailMe.getAbsolutePath());
					moreLogs.addAll(Arrays.asList(FileTail.tail(myLog.getAbsolutePath(), 100)));
				}
				results = moreLogs.toArray(new String[0]);
			}
			catch (Exception err)
			{
				results[0] = "Error reading log file " + StringEscapeUtils.escapeJson(err.getMessage());
			}
		}
		else
		{
			results[0] = null;
		}
		return results;
	}

	public static String getFirstDir(File theDir)
	{
		String name = null;
		if (theDir.exists())
		{
			File[] ls = theDir.listFiles(File::isDirectory);
			if (null != ls)
			{
				name = ls[0].getName();
			}
		}
		return name;
	}

	public static String getDefaultDir(File theResultDir, String theJobId)
	{
		// jobid, algorithm, diagram, subtype, diagram
		String level1 = theJobId;
		String level2 = "PCA";
		String level3 = null;
		String level4 = null;
		String level5 = null;
		try
		{
			// check for PCA directory and batch dir
			level3 = getFirstDir(new File(new File(theResultDir, level1), level2));
			if (null == level3)
			{
				// if no PCA, redo level 2 and 3
				level2 = getFirstDir(new File(theResultDir, level1));
				level3 = getFirstDir(new File(new File(theResultDir, level1), level2));
			}
			level4 = getFirstDir(new File(new File(new File(theResultDir, level1), level2), level3));
			level5 = getFirstDir(new File(new File(new File(new File(theResultDir, level1), level2), level3), level4));
		}
		catch (Exception exp)
		{
			// TODO: replace eating exception with new version of default
			level2 = null;
			level3 = null;
			level4 = null;
			level5 = null;
		}
		// build return dir
		String ret = "";
		if (null != level1)
		{
			ret = ret + level1 + "/";
			if (null != level2)
			{
				ret = ret + level2 + "/";
				if (null != level3)
				{
					ret = ret + level3 + "/";
					if (null != level4)
					{
						ret = ret + level4 + "/";
						if (null != level5)
						{
							ret = ret + level5 + "/";
						}
					}
				}
			}
		}
		return ret;
	}

	static public File findNewestFile(File theDir, String thePostfix)
	{
		File newest = null;
		TreeSet<File> fileList = new TreeSet<>();
		File[] tmp = theDir.listFiles();
		if (null != tmp)
		{
			for (File nf : tmp)
			{
				if (nf.isFile())
				{
					if (nf.getName().endsWith(thePostfix))
					{
						fileList.add(nf);
					}
				}
			}
			newest = fileList.descendingSet().first();
		}
		return newest;
	}

	public static void copyToWebsite(String theJob, HttpServletRequest theRequest, HttpServlet theServlet) throws IOException
	{
		File jobDir = new File(MBAUtils.M_OUTPUT, theJob);
		theServlet.log("theJob=" + theJob);
		theServlet.log("jobDir=" + jobDir);
		ArrayList<String> websiteTxt = new ArrayList<>();
		// links do not work, copy instead
		File resultsZip = findNewestFile(jobDir, "-results.zip");
		File dataZip = findNewestFile(jobDir, "-data.zip");
		File webResults = new File(MBAUtils.M_WEBSITE, theJob + "-results.zip");
		File webData = new File(MBAUtils.M_WEBSITE, theJob + "-data.zip");
		theServlet.log("copyToWebsite from resultsZip=" + resultsZip + " to webResults=" + webResults);
		Files.copy(resultsZip.toPath(), webResults.toPath());
		theServlet.log("copyToWebsite from dataZip=" + dataZip + " to webData=" + webData);
		Files.copy(dataZip.toPath(), webData.toPath());
		//File webLink = new File(MBAUtils.M_WEBSITE, theJob + ".zip");
		//Files.createSymbolicLink(webLink.toPath(), zipFile.toPath());
		//
		// websiteTxt first line, path to directory
		//
		websiteTxt.add(jobDir.toString());
		//
		// websiteTxt second line, URL to JOB
		//
		//http://mba_service:8080/MBA/MBA/JOBupdate?jobId=1516904638750&status=MBATCH_SUCCESS
		websiteTxt.add(buildUrlToJob(theRequest, theServlet, theJob));
		//
		// websiteTxt third line, URL to WEBSITE
		//
		//http://mba_service:8080/MBA/BEV?id=XXX&index=YYY
		websiteTxt.add(buildUrlToWebsite(theRequest, theServlet, theJob));
		Files.write(new File(jobDir, "website.txt").toPath(), websiteTxt);
	}

	public static String buildUrlToJob(HttpServletRequest request, HttpServlet theServlet, String theJob) throws IOException
	{
		return request.getScheme() + "://" + MBAproperties.getProperty("MBA_URL", theServlet) + request.getContextPath() + "/newjob.html?job=" + theJob;
	}

	public static String buildUrlToWebsite(HttpServletRequest request, HttpServlet theServlet, String theJob) throws IOException
	{
		return request.getScheme() + "://" + MBAproperties.getProperty("BEV_URL", theServlet) + "/view?id=" + theJob + "&index=MBA_JOB&alg=PCA%2B&lvl1=BatchId";
	}
}
