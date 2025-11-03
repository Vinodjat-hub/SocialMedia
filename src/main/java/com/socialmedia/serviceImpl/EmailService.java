package com.socialmedia.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.socialmedia.num.OtpType;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	/**
	 * Sends an OTP email to the given recipient, customized by OtpType.
	 *
	 * @param toEmail recipient email address
	 * @param otp     generated OTP code
	 * @param otpType type of OTP (REGISTRATION or FORGET_PASSWORD)
	 */
	public void sendOtpEmail(String toEmail, String otp, OtpType otpType) {
		try {
			System.err.println("📧 Sending OTP to: " + toEmail + " | OTP: " + otp + " | Type: " + otpType);

			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(toEmail);

			// ✅ Dynamic subject and message based on OtpType
			String subject = getSubjectByOtpType(otpType);
			String message = getMessageByOtpType(otp, otpType);

			mailMessage.setSubject(subject);
			mailMessage.setFrom("Social Media Platform <noreply@socialmedia.com>");
			mailMessage.setText(message);

			// ✅ Send email
			mailSender.send(mailMessage);
			System.out.println("✅ OTP email sent successfully to " + toEmail);

		} catch (Exception e) {
			System.err.println("❌ Error sending OTP email: " + e.getMessage());
		}
	}

	// 🔹 Choose subject line based on OTP type
	private String getSubjectByOtpType(OtpType otpType) {
		switch (otpType) {
		case REGISTRATION:
			return "Verify Your Social Media Account";
		case FORGET_PASSWORD:
			return "Reset Your Password - Social Media Platform";
		default:
			return "Your OTP Code - Social Media Platform";
		}
	}

	// 🔹 Choose message content based on OTP type
	private String getMessageByOtpType(String otp, OtpType otpType) {
		String purposeMessage;

		switch (otpType) {
		case REGISTRATION:
			purposeMessage = "Thank you for registering! Please verify your Social Media account using the OTP below.";
			break;
		case FORGET_PASSWORD:
			purposeMessage = "You requested to reset your password. Use the OTP below to proceed.";
			break;
		default:
			purposeMessage = "Use the OTP below to complete your action.";
		}

		return "Dear User,\n\n" + purposeMessage + "\n\n" + "🔹 OTP: " + otp + "\n\n"
				+ "This OTP will expire in 5 minutes.\n\n" + "Best Regards,\nSocial Media Platform Team\n"
				+ "-----------------------------------------------------\n"
				+ "⚠️ Do not share this OTP with anyone for security reasons.";
	}
}
