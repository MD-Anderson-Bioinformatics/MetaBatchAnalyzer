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
import edu.mda.bcb.samval.SampleValidatorException;
import edu.mda.bcb.samval.SamplesValidationUtil;
import edu.mda.bcb.samval.matrix.Matrix;
import edu.mda.bcb.mba.status.JOB_STATUS;
import edu.mda.bcb.mba.status.JobStatus;
import edu.mda.bcb.samval.matrix.Builder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

/**
 *
 * @author Tod-Casasent
 */
@MultipartConfig
@WebServlet(name = "UploadBatch", urlPatterns =
{
	"/UploadBatch"
})
public class UploadBatch extends MBAServletMixin
{

	public UploadBatch()
	{
		super("application/text;charset=UTF-8", true, UploadBatch.class);
	}

	/**
	 * Message returned from saveFileUpload to indicate success. All other
	 * values are interpreted as failure, likely providing an explanation as to
	 * the failure.
	 */
	private static final String successfulUpload = "FILE UPLOAD: SUCCESS";

	/**
	 * Message returned from matrixPostProcessing to indicate success. All other
	 * values are interpreted as failure, likely providing an explanation as to
	 * the failure.
	 */
	private static final String successfulPostProcessing = "POST PROCESS: SUCCESS";

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		boolean keepOriginal = paramStringToBool(request.getParameter("keepOriginal"));
		// No rectangle flag should be logically inverted
		boolean noRectangle = !paramStringToBool(request.getParameter("noRectangle"));
		boolean sortRows = paramStringToBool(request.getParameter("sortRows"));
		boolean sortCols = paramStringToBool(request.getParameter("sortCols"));
		boolean filterMatrix = paramStringToBool(request.getParameter("filterMatrix"));
		boolean createMissingBatches = paramStringToBool(request.getParameter("createMissingBatches"));
		String isAlternate = request.getParameter("isAlternate");
		boolean isAlternateP = isAlternate.equals("YES");
		File jobDir = new File(MBAUtils.M_OUTPUT, jobId);
		File zipDataDir = new File(jobDir, "ZIP-DATA");
		File zipDataOriginalDir = new File(zipDataDir, "original");
		zipDataOriginalDir.mkdirs();
		final String savePath = new File(zipDataOriginalDir, (isAlternateP ? "batches2.tsv" : "batches.tsv")).getAbsolutePath();
		log("passed in jobId is " + jobId);
		log("passed in isAlternate is " + isAlternate);
		log("fileLocation is " + jobDir.getAbsolutePath());
		/**
		 * message will be the ultimate response text. A message equal to the
		 * jobId is interpreted as successful file upload, and anything else is
		 * interpreted as failure (with failure message).
		 */
		String message = jobId;
		try
		{
			String result1 = null;
			String result2 = null;
			result1 = saveFileUpload(request.getPart("file"), savePath, this);
			if (result1.equals(successfulUpload))
			{
				result2 = batchesPostProcessing(savePath, isAlternateP, keepOriginal, noRectangle,
						sortRows, sortCols, filterMatrix, createMissingBatches);
			}
			if (successfulUpload.equals(result1) && successfulPostProcessing.equals(result2))
			{
				if (isAlternateP)
				{
					JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_SECONDARY_DONE, request, this);
				}
				else
				{
					JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_PRIMARY_DONE, request, this);
				}
			}
			else
			{
				message = "";
				if (!result1.equals(successfulUpload))
				{
					message += "File Upload Failure: " + result1 + ". ";
				}
				if (!result2.equals(successfulPostProcessing))
				{
					message += "File Post-Processing Failure: " + result2;
				}
			}
		}
		catch (IOException | ServletException exp)
		{
			log("Problem in file upload for job " + jobId + ". Error: " + exp.getMessage(), exp);
			throw new Exception("Problem in file upload for job " + jobId + ". Error: " + exp.getMessage(), exp);
		}
		theBuffer.append(message);
	}

	// Utility to interpret "false" and "true" flag argument values from request parameters.
	public static boolean paramStringToBool(String param)
	{
		return param.equals("true");
	}

	// TODO: Evaluate whether its possible/wise to call the saveFileUpload in UploadBatch/UploadMatrix rather than have this method defined here
	public static String saveFileUpload(Part thePart, String theSavePath, HttpServlet theServlet)
	{
		String message = "";
		OutputStream out = null;
		InputStream filecontent = null;
		try
		{
			//long size = 0;
			//String machineName = InetAddress.getLocalHost().getHostName();
			out = new FileOutputStream(theSavePath);
			filecontent = thePart.getInputStream();

			int read = 0;
			final byte[] bytes = new byte[1024];

			while ((read = filecontent.read(bytes)) != -1)
			{
				//size = size + 1024;
				out.write(bytes, 0, read);
			}
			theServlet.log("File mbang uploaded to " + theSavePath);
			message = successfulUpload;
		}
		catch (FileNotFoundException fne)
		{
			message = "You either did not specify a file to upload or are trying to upload a file to a protected or nonexistent location. "
					+ fne.getMessage();
			theServlet.log("Problems during file upload. Error: " + fne.getMessage(), fne);
			//response.setStatus(500);
			//response.sendError(500, exp.getMessage());
		}
		catch (Exception exp)
		{
			message = exp.getMessage();
			theServlet.log("Problems during file upload. Error: " + exp.getMessage(), exp);
			//response.setStatus(500);
			//response.sendError(500, exp.getMessage());
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (Exception ignore)
				{
					//
				}
			}
			if (filecontent != null)
			{
				try
				{
					filecontent.close();
				}
				catch (Exception ignore)
				{
					//
				}
			}
		}
		return message;
	}

	public static String batchesPostProcessing(
			String batchesPath,
			boolean isAlternate,
			boolean keepOriginalFlag,
			boolean noRectangleFlag,
			boolean sortRowsFlag,
			boolean sortColsFlag,
			boolean filterMatrixFlag,
			boolean createMissingBatchesFlag) throws Exception
	{
		try
		{
			File batchesFile = new File(batchesPath);
			String baseDir = batchesFile.getParent();
			String matrixPath;
			if (isAlternate)
			{
				matrixPath = new File(baseDir, "matrix_data2.tsv").getAbsolutePath();
			}
			else
			{
				matrixPath = new File(baseDir, "matrix_data.tsv").getAbsolutePath();
			}
			Matrix batches = new Builder(batchesPath)
					.allowNonRectangle(noRectangleFlag)
					.build();
			Matrix matrix = new Builder(matrixPath).build();
			boolean matrixChanged = false;
			boolean batchesChanged = false;
			if (!filterMatrixFlag && !createMissingBatchesFlag)
			{
				boolean validSamples = SamplesValidationUtil.containsSamples(batches, matrix.getColumns());
				if (!validSamples)
				{
					throw new SampleValidatorException("Batches file did not contain samples found in matrix file");
				}
			}
			else
			{
				if (filterMatrixFlag)
				{
					Matrix.filterMatrix(matrix, batches.getRowSet(), 1);
					matrixChanged = true;
				}
				if (createMissingBatchesFlag)
				{
					SamplesValidationUtil.createMissingBatchEntries(batches, matrix.getColumns());
					batchesChanged = true;
				}
				else
				{
					Matrix.filterMatrix(batches, matrix.getColumnSet(), 0);
					batchesChanged = true;
				}
			}
			if (sortRowsFlag)
			{
				batches.sortRows();
				batchesChanged = true;
			}
			if (sortColsFlag)
			{
				batches.sortColumns();
				batchesChanged = true;
			}
			if (matrixChanged)
			{
				matrix.sortRows();
				matrix.sortColumns();
				updatePostProcessedFile(matrix, keepOriginalFlag);
			}
			if (batchesChanged)
			{
				updatePostProcessedFile(batches, keepOriginalFlag);
			}
			return successfulPostProcessing;
		}
		catch (Exception e)
		{
			return e.getMessage();
		}
	}

	private static void updatePostProcessedFile(Matrix m, boolean keepOriginal) throws IOException
	{
		String mPath = m.getPath();
		File mFile = new File(mPath);
		String mName = mFile.getName();
		String baseDir = mFile.getParent();
		String tempPath = new File(baseDir, mName.replace(".tsv", "_temp.tsv")).getAbsolutePath();
		m.write(tempPath);
		if (keepOriginal)
		{
			File originalFile = new File(mPath.replace(".tsv", "_original.tsv"));
			mFile.renameTo(originalFile);
		}
		Files.copy(Paths.get(tempPath), Paths.get(mPath), StandardCopyOption.REPLACE_EXISTING);
		new File(tempPath).delete();
	}
}
