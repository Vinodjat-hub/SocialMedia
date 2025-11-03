package com.dollop.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	public void sendOtpEmail(String toEmail, String otp) {
		try {
			System.err.println("toEmail <===> " + toEmail + " otp <===> " + otp);
			SimpleMailMessage mailMessage = new SimpleMailMessage();

			// ✅ Set recipient
			mailMessage.setTo(toEmail);

			// ✅ Set subject
			mailMessage.setSubject("Your OTP Code");

			// ✅ Set custom sender name with email
			mailMessage.setFrom("Real Time Order Trcking <vinodjat8818@gmail.com>");

			// ✅ Custom message + OTP
			mailMessage.setText("Dear user,\n\n" + "Please use the following OTP to verify your account:\n\n" + otp
					+ "\n\n" + "This OTP will expire in 5 minutes.\n\n" + "Regards,\nReal Time Order Tracking Team");

			// ✅ Send email
			mailSender.send(mailMessage);
			System.out.println("✅ OTP email sent to " + toEmail);
		} catch (Exception e) {
			System.out.println("❌ Error sending OTP email: " + e.getMessage());
		}
	}

}
