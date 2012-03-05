package org.ntu.rtsearch.utils;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class EmailUtil {

	/**
	 * send email to admin
	 * 
	 * @param e
	 * @throws EmailException
	 */
	public static void sendEmail(Exception e) {
		try {
			SimpleEmail email = new SimpleEmail();
			email.setHostName("smtp.gmail.com");
			email.setAuthentication("hfhimage@gmail.com", "lovehui1314");
			email.setSSL(true);
			email.setSslSmtpPort("465");
			email.setFrom("137045001@qq.com");
			email.addTo("hfhimage@gmail.com");
			email.setSubject("data collection error");
			email.setMsg(e.toString());
			email.send();
		} catch (Exception ex) {

		}
	}
}
