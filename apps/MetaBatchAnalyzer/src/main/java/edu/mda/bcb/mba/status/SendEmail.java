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

package edu.mda.bcb.mba.status;

import edu.mda.bcb.mba.servlets.MBAproperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author Tod-Casasent
 */
public class SendEmail
{

	static public void sendEmail(String theJobId, HttpServlet theServlet) throws IOException, MessagingException
	{
		HashMap<String, String> map = JobStatus.getJobMap(theJobId);
		String email = map.get("email");
		MBAproperties.getResponseString(theServlet);
		String host = MBAproperties.getProperty("smtpHost", theServlet);
		String port = MBAproperties.getProperty("smtpPort", theServlet);
		if (((null != email) && (!"".equals(email)))
				&& ((null != host) && (!"".equals(host)))
				&& ((null != port) && (!"".equals(port))))
		{
			String subject = "MBA: Update for " + theJobId + " from " + MBAproperties.getProperty("serverTitle", theServlet);
			/////////////////// 
			String emailBody = "";
			emailBody = emailBody + "Update for MetaBatch Analyzer from " + MBAproperties.getProperty("serverTitle", theServlet) + "\n\n";
			emailBody = emailBody + "Update for Job Id " + theJobId + "\n\n";
			emailBody = emailBody + "Tagged as " + map.get("tag") + "\n\n";
			emailBody = emailBody + "Status is " + map.get("status") + "\n\n";
			emailBody = emailBody + "Status message is " + map.get("message") + "\n\n";
			emailBody = emailBody + "Owner is " + map.get("owner") + "\n\n";
			emailBody = emailBody + "\n\n";
			emailBody = emailBody + "Last log file tail is:" + "\n\n";
			emailBody = emailBody + "\n\n";
			emailBody = emailBody + map.get("tail") + "\n\n";
			///////////////////
			internalSendEmail(host, port, email, subject, emailBody, theServlet);
		}
	}

	static protected void internalSendEmail(String theServer, String thePort, String theEmail, String theSubject, String theBody, HttpServlet theServlet) throws AddressException, MessagingException
	{
		if (null != theServlet)
		{
			theServlet.log("Sending email to " + theEmail);
		}
		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", theServer);
		properties.setProperty("mail.smtp.port", thePort);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		// Create a default MimeMessage object.
		MimeMessage message = new MimeMessage(session);

		// Set From: header field of the header.
		message.setFrom(new InternetAddress(theEmail));

		// Set To: header field of the header.
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(theEmail));

		// Set Subject: header field
		message.setSubject(theSubject);

		// Now set the actual message
		message.setText(theBody);

		// Send message
		Transport.send(message);
		if (null != theServlet)
		{
			theServlet.log("Sent email to " + theEmail);
		}
	}
}
