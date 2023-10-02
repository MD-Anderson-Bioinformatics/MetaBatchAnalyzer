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

import edu.mda.bcb.mba.utils.MBAUtils;
import edu.mda.bcb.mba.servlets.MBAServletMixin;
import edu.mda.bcb.mba.status.JobStatus;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "JOBnew", urlPatterns =
{
	"/JOBnew"
})
public class JOBnew extends MBAServletMixin
{
	public JOBnew()
	{
		super("application/text;charset=UTF-8", true, JOBnew.class);
	}

	static synchronized String newJobId()
	{
		try
		{
			Thread.sleep(1);
		}
		catch(Exception ignore)
		{
			// ignore
		}
		return Long.toString(System.currentTimeMillis());
	};

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = newJobId();
		File jobDir = new File(MBAUtils.M_OUTPUT, jobId);
		log("jobDir creation " + jobDir.getAbsolutePath());
		jobDir.mkdirs();
		if (jobDir.exists())
		{
			log("jobDir confirmed " + jobDir.getAbsolutePath());
		}
		////////////////////////////////////////////////////////////////////////
		// ZIP Results Directory
		////////////////////////////////////////////////////////////////////////
		File zipResultsDir = new File(jobDir, "ZIP-RESULTS");
		log("zipResultsDir creation " + zipResultsDir.getAbsolutePath());
		zipResultsDir.mkdirs();
		// TODO: this is a hack - sometimes mkdir seems to fail "magically"
		if (zipResultsDir.exists())
		{
			log("zipResultsDir confirmed " + zipResultsDir.getAbsolutePath());
		}
		// write source id and version stamp and type
		Files.write(new File(zipResultsDir, "source_id.txt").toPath(), "MBA-RUN".getBytes(StandardCharsets.UTF_8));
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HHmm");
		String timestamp = dateFormat.format(calendar.getTime());
		Files.write(new File(zipResultsDir, "version_stamp.txt").toPath(), timestamp.getBytes(StandardCharsets.UTF_8));
		Files.write(new File(zipResultsDir, "version_type.txt").toPath(), "MBA-DATA".getBytes(StandardCharsets.UTF_8));
		////////////////////////////////////////////////////////////////////////
		// ZIP Data Directory
		////////////////////////////////////////////////////////////////////////
		File zipDataDir = new File(jobDir, "ZIP-DATA");
		log("zipDataDir creation " + zipDataDir.getAbsolutePath());
		zipDataDir.mkdirs();
		if (zipDataDir.exists())
		{
			log("zipDataDir confirmed " + zipDataDir.getAbsolutePath());
		}
		Files.copy(new File(zipResultsDir, "source_id.txt").toPath(), new File(zipDataDir, "source_id.txt").toPath());
		Files.copy(new File(zipResultsDir, "version_stamp.txt").toPath(), new File(zipDataDir, "version_stamp.txt").toPath());
		Files.copy(new File(zipResultsDir, "version_type.txt").toPath(), new File(zipDataDir, "version_type.txt").toPath());
		////////////////////////////////////////////////////////////////////////
		File zipDataOriginalDir = new File(zipDataDir, "original");
		zipDataOriginalDir.mkdir();
		JobStatus.createNewJob(jobId, request.getRemoteUser(), this);
		log("fileLocation is " + jobDir.getAbsolutePath());
		theBuffer.append(jobId);
	}
}
