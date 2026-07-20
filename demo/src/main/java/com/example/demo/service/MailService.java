package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    // Scheduled method to send email 
    public void sendMail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("vn119970@gmail.com");

        mailSender.send(message);
        logger.info("Email sent successfully without attachments");
    }


    // Method to send email with attachments while uploading csv with errors
    public void sendMailWithAttachments(String to, String subject, String body, byte[] attachmentCsv,byte[] attachmentPdf) {
        try {
            // Create a multipart message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            logger.info("Preparing email with attachments: to={}, subject={}", to, subject);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            helper.setFrom("vn119970@gmail.com");
            helper.addAttachment("data.csv", new ByteArrayResource(attachmentCsv));
            helper.addAttachment("report.pdf", new ByteArrayResource(attachmentPdf));
            mailSender.send(message);
            logger.info("Email sent successfully with attachments");
        } catch (MessagingException e) {
            e.printStackTrace();
            logger.error("Failed to send email with attachments: " + e.getMessage());
             throw new RuntimeException("Failed to send email with attachments: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String to, String newPassword) {
        String subject = "Password Reset Notification";
        String body = "Your password has been reset. Your new password is: " + newPassword;
        sendMail(to, subject, body);
    }
}