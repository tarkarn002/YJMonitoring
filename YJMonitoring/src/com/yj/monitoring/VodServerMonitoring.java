package com.yj.monitoring;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class VodServerMonitoring {
	
	// MAIL SERVER INFO
	private static final String SMTP_IP_ADDRESS = "180.150.229.158";	// Mail Server IP
	private static final int 	SMTP_PORT	 	= 25;					// Mail Server Port
	
	// VOD SERVER INFO
	private static final String PROTOCOL 		= "http://";			// Protocol 명
	private static final String VOD_SERVER_IP 	= "180.150.229.152"; 	// VOD SERVER IP 주소
	
	public static void main(String[] args) {
		VodServerMonitoring vodMonitor = new VodServerMonitoring();
		FileWriter log = null;
		
		try {
			log = new FileWriter("C:\\ScheduleJob\\Monitoring\\VodLog.log");
			vodMonitor.checkVodServerStatus(vodMonitor, log);
			
		} catch (Exception e) {
			System.out.println("Log File Output Error << ");
			
		} finally {
			if(log != null) {try {log.close();} catch (IOException e) {e.printStackTrace();}}
		}
	}
	
	
	/**
	 * VOD 서버가 살아있는지 상태를 체크한다.
	 * @throws IOException 
	 */
	private void checkVodServerStatus(VodServerMonitoring vodMonitor, FileWriter log) throws IOException {
		
		HttpClient httpClient = new DefaultHttpClient();
		boolean isAlive = true;
		
		try {
			HttpGet httpget = new HttpGet(PROTOCOL + VOD_SERVER_IP);

			System.out.println("executing request " + httpget.getURI());
			log.write("executing request " + httpget.getURI() + "\n");
			
			HttpResponse response = httpClient.execute(httpget);

			// 응답 결과
			System.out.println("HTTP Status Code [" + response.getStatusLine().getStatusCode() + "]");
			log.write("HTTP Status Code [" + response.getStatusLine().getStatusCode() + "]\n");
			
			// HTTP Code 200 이 아닌경우 메일 발송처리.
			if(HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
				isAlive = false;
			}

			httpget.abort();
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			isAlive = false;
			System.out.println(" Exception Occurred!! ClientProtocolException << ");
			log.write("Exception Occurred!! ClientProtocolException << " + e.getMessage() + "\n");
			
		} catch (IOException e) {
			e.printStackTrace();
			isAlive = false;
			System.out.println(" Exception Occurred!! IOException << " + e.getMessage());
			log.write("Exception Occurred!! IOException << " + e.getMessage() + "\n");
			
		} finally {
			httpClient.getConnectionManager().shutdown();
			log.write("isAlive [" + isAlive + "]\n");
			
			if(!isAlive) {
				
				// Send Mail 
				vodMonitor.sendEmail("tarkarn002@nate.com", log); 	// 조광재
				vodMonitor.sendEmail("developer@yj.co.kr", log); 	// 이동주
				
				// TODO LIST :: Send SMS..
				
			}
		}
	}
	
	
	/**
	 * 에러발생여부를 Email로 발송한다.
	 * @throws IOException 
	 */
	private void sendEmail(String mailAddr, FileWriter log) throws IOException {
		
		Email email = new SimpleEmail();
		
		try {
			email.setHostName(SMTP_IP_ADDRESS);
			email.setSmtpPort(SMTP_PORT);
			email.setAuthenticator(new DefaultAuthenticator("admin1", "yjcyber1"));
			email.setFrom("admin@yj.co.kr","YJ 모니터링");
			email.setSubject("VOD SERVER DEAD << ");
			email.setMsg("[180.150.229.152] VOD SERVER 장애 발생\n확인 해 주세요.");
			email.addTo(mailAddr);
			email.send();
			
			log.write("Mail Send Success ["+mailAddr+"]\n");
			
		} catch (EmailException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO LIST::
	 * 에러발생여부를 SMS로 발송한다.
	 */
	private void sendSMS() {
		
	}
}
