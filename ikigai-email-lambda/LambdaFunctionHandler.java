package com.amazonaws.lambda.demo;

import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LambdaFunctionHandler implements RequestHandler<SQSEvent, String> {

	@Override
	public String handleRequest(SQSEvent input, Context context) {
		context.getLogger().log("Input: " + input);

        List<SQSMessage> records = input.getRecords();
		
		JsonObject request = new JsonParser().parse(new Gson().toJson(input)).getAsJsonObject();
		
		String task = records.get(0).getBody();
		
		String taskCase = task.split(";")[0];
		String taskSubject = task.split(";")[1];
		String taskRecipients = task.split(";")[2];
		String taskSubjectData = task.split(";")[3];
		String from = "tj.lewis1025@gmail.com";
		String to = "tlewis@student.neumont.edu";
		String host = "smtp.gmail.com";
		final String user = from;
		final String password = "Kayfdsa032414$!";
		Properties properties = System.getProperties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.starttls.enable", "true");
		Session session = Session.getDefaultInstance(properties, null);
		try {
			Transport transport = session.getTransport("smtp");
			transport.connect(host, user, password);
			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(from));

			//For testing purposes
//			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//Actual Production use
			String[] allRecipients = taskRecipients.split("#");
			for (String recipient : allRecipients) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
			}

			message.setSubject(taskSubject);
			
			StringBuilder sb = new StringBuilder();
			switch(taskCase) {
				case "PASSWORDCHANGE":
					sb.append("<h1>Your password has been updated.\r\n</h1>");
					break;
				case "PASSWORDRESET":
					sb.append("<h1>Your password has been reset to: " + taskSubjectData + "\r\n</h1>");
					break;
				case "TASKCREATED":
					sb.append("<h1>A task has been created for your account: " + taskSubjectData + "\r\n</h1>");
					break;
				case "COMPLETEDTASK":
					sb.append("<h1>A task has been completed for your account: " + taskSubjectData + "\r\n</h1>");
					break;
			}
			
			message.setContent(sb.toString().trim(), "text/html");


			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			return setResponseJSON(
					"{ \"Status\": 200, \"Message_Header\": \"OK\", \"Message\": \"Email sent successfully\" }");
		} catch (MessagingException mex) {
			return setResponseJSON(
					"{ \"Status\": 500, \"Message_Header\": \"ERROR\", \"Message\": \"Internal Server Error" + mex.fillInStackTrace() + "\" }");
		}
	}

	private String setResponseJSON(String content) {
		return new Gson().toJson(new JsonParser().parse(content).getAsJsonObject());
	}

}
