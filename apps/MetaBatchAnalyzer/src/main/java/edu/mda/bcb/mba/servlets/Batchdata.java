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
import edu.mda.bcb.mba.processes.BatchdataObj;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.mda.bcb.mba.status.JobStatus;
import edu.mda.bcb.mba.utils.ScanCheck;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeMap;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "Batchdata", urlPatterns =
{
	"/Batchdata"
})
public class Batchdata extends MBAServletMixin
{
	public Batchdata()
	{
		super("application/json;charset=UTF-8", true, Batchdata.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		// return to user handled in parent
		String jobId = request.getParameter("jobId");
		ScanCheck.checkForMetaCharacters(jobId);
		log("passed in jobId is " + jobId);
		JobStatus.checkJobId(jobId);
		String isAlternate = request.getParameter("isAlternate");
		ScanCheck.checkForYesNo(isAlternate);
		// NO, YES, YES-IGNORE
		log("passed in isAlternate is " + isAlternate);
		boolean isAltFlag = isAlternate.startsWith("YES");
		Collection<BatchdataObj> batches = loadBatchdata((new File(new File(new File(MBAUtils.M_OUTPUT, jobId), "ZIP-DATA"), "original")), isAltFlag);
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		String json = gson.toJson(batches);
		json = json.replace("}},", "}},\n");
		theBuffer.append(json);
	}

	protected Collection<BatchdataObj> loadBatchdata(File theDownloadDir, boolean theIsAlternateP) throws Exception
	{
		TreeMap<String, BatchdataObj> batchTypesTo = new TreeMap<>();
		if (!theDownloadDir.exists())
		{
			throw new Exception("Download directory not found:" + theDownloadDir);
		}
		else
		{
			File batchFile = null;
			if(false == theIsAlternateP)
			{
				batchFile = new File(theDownloadDir, "batches.tsv");
				if (!batchFile.exists())
				{
					throw new Exception("Primary Batch file not found:" + batchFile);
				}
			}
			else
			{
				batchFile = new File(theDownloadDir, "batches2.tsv");    
				if (!batchFile.exists())
				{
					// won't always be there
					batchFile = null;
				}
			}
			if (null!=batchFile)
			{
				// batch type name, batch names with count
				// read the file
				ArrayList<String> headers = null;
				try (BufferedReader br = new BufferedReader(new FileReader(batchFile)))
				{
					String line;
					while ((line = br.readLine()) != null)
					{
						if (null==headers)
						{
							// populate headers (batch types)
							headers = new ArrayList<>();
							headers.addAll(Arrays.asList(line.split("\t", -1)));
							for(String header : headers)
							{
								batchTypesTo.put(header, new BatchdataObj(header));
							}
						}
						else
						{
							// populate batches
							String [] splitted = line.split("\t", -1);
							for(int index=0; index<headers.size(); index++ )
							{
								String batchtype = headers.get(index);
								String batch = splitted[index];
								BatchdataObj batchtypeObj = batchTypesTo.get(batchtype);
								Integer batchcount = batchtypeObj.mBatches.get(batch);
								if (null==batchcount)
								{
									// use auto int to Integer
									batchcount = 1;
								}
								else
								{
									// use auto add int to Integer
									batchcount += 1;
								}
								batchtypeObj.mBatches.put(batch, batchcount);
							}
						}
					}
				}
			}
		}
		return batchTypesTo.values();
	}
}
