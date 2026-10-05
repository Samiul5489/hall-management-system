package com.hallmanagement.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmailService {

    private static final Properties config = new Properties();
    private static final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "EmailService-Worker");
        t.setDaemon(true);
        return t;
    });

    private static boolean isConfigured = false;
    private static String senderEmail = "";
    private static String senderPassword = "";
    private static String senderName = "RUET Hall Management System";

    static {
        loadConfiguration();
    }

    public static void loadConfiguration() {
        try (InputStream is = EmailService.class.getResourceAsStream("/email.properties")) {
            if (is != null) {
                config.load(is);
                senderEmail = config.getProperty("mail.sender.email", "").trim();
                senderPassword = config.getProperty("mail.sender.password", "").trim().replace(" ", "");
                senderName = config.getProperty("mail.sender.name", "RUET Hall Management System").trim();

                if (!senderEmail.isEmpty() && !senderPassword.isEmpty()) {
                    isConfigured = true;
                    System.out.println("[EmailService] Configured with sender email: " + senderEmail);
                } else {
                    isConfigured = false;
                    System.out.println("[EmailService] Notice: Sender email or password not yet filled in email.properties.");
                }
            } else {
                System.err.println("[EmailService] Warning: /email.properties resource not found on classpath.");
            }
        } catch (Exception e) {
            System.err.println("[EmailService] Error loading email.properties: " + e.getMessage());
        }
    }

    public static boolean isConfigured() {
        return isConfigured && !senderEmail.isEmpty() && !senderPassword.isEmpty();
    }

    public static CompletableFuture<Boolean> sendOtpEmailAsync(String recipientEmail, String recipientName, String otpCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendOtpEmailSync(recipientEmail, recipientName, otpCode);
            } catch (Exception e) {
                System.err.println("[EmailService] Failed to send OTP email to " + recipientEmail + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    public static boolean sendOtpEmailSync(String recipientEmail, String recipientName, String otpCode) throws Exception {
        if (!isConfigured()) {
            System.out.println("[EmailService] [OFFLINE/UNCONFIGURED MODE] OTP for " + recipientEmail + " is: [" + otpCode + "]");
            System.out.println("[EmailService] (To receive real emails in your inbox, set your Gmail address and 16-character App Password in src/main/resources/email.properties)");
            return true;
        }

        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", config.getProperty("mail.smtp.host", "smtp.gmail.com"));
        mailProps.put("mail.smtp.port", config.getProperty("mail.smtp.port", "587"));
        mailProps.put("mail.smtp.auth", config.getProperty("mail.smtp.auth", "true"));
        mailProps.put("mail.smtp.starttls.enable", config.getProperty("mail.smtp.starttls.enable", "true"));
        mailProps.put("mail.smtp.ssl.protocols", config.getProperty("mail.smtp.ssl.protocols", "TLSv1.2"));
        mailProps.put("mail.smtp.ssl.trust", "*");
        mailProps.put("mail.smtp.connectiontimeout", "15000");
        mailProps.put("mail.smtp.timeout", "15000");

        Session session = Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail, senderName));
        message.setReplyTo(new Address[]{new InternetAddress(senderEmail)});
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject("RUET Hall Management: Verification Code " + otpCode);
        message.setSentDate(new java.util.Date());
        message.setHeader("X-Mailer", "JavaMail");

        String htmlContent = buildOtpHtmlBody(recipientName, otpCode);
        message.setContent(htmlContent, "text/html; charset=UTF-8");

        System.out.println("[EmailService] Connecting to SMTP server and dispatching OTP to " + recipientEmail + "...");
        Transport.send(message);
        System.out.println("[EmailService] ✓ Real OTP Email successfully delivered to: " + recipientEmail);
        return true;
    }

    private static String buildOtpHtmlBody(String recipientName, String otpCode) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<style>"
                + "body { font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, Arial, sans-serif; background-color: #0b1523; color: #e2e8f0; margin: 0; padding: 24px; }"
                + ".container { max-width: 540px; margin: 0 auto; background: #13243f; border-radius: 12px; border: 1px solid rgba(255,255,255,0.1); overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }"
                + ".header { background: linear-gradient(135deg, #1e88e5, #0d47a1); padding: 24px; text-align: center; }"
                + ".header h1 { color: #ffffff; margin: 0; font-size: 20px; letter-spacing: 0.5px; font-weight: bold; }"
                + ".header p { color: #bbdefb; margin: 6px 0 0 0; font-size: 13px; }"
                + ".content { padding: 32px 28px; }"
                + ".greeting { font-size: 16px; font-weight: 600; color: #90caf9; margin-bottom: 12px; }"
                + ".text { font-size: 14px; line-height: 1.6; color: #cbd5e1; margin-bottom: 24px; }"
                + ".otp-card { background: #0a1628; border: 2px dashed #1e88e5; border-radius: 10px; padding: 20px; text-align: center; margin: 24px 0; }"
                + ".otp-label { font-size: 12px; font-weight: 700; color: #90caf9; letter-spacing: 1.5px; text-transform: uppercase; margin-bottom: 8px; }"
                + ".otp-code { font-size: 34px; font-weight: 900; letter-spacing: 10px; color: #ffeb3b; font-family: monospace, Consolas; margin: 6px 0; }"
                + ".otp-expiry { font-size: 12px; color: #94a3b8; margin-top: 8px; }"
                + ".warning { background: rgba(239, 68, 68, 0.12); border-left: 4px solid #ef4444; padding: 12px 16px; border-radius: 4px; font-size: 12.5px; color: #fca5a5; margin-top: 24px; }"
                + ".footer { padding: 20px; text-align: center; font-size: 12px; color: #64748b; border-top: 1px solid rgba(255,255,255,0.06); background: #0b1523; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h1>RAJSHAHI UNIVERSITY OF ENGINEERING &amp; TECHNOLOGY</h1>"
                + "<p>Hall Management &amp; Student Residence System</p>"
                + "</div>"
                + "<div class='content'>"
                + "<div class='greeting'>Hello " + (recipientName != null ? recipientName : "User") + ",</div>"
                + "<div class='text'>You requested a password reset verification code for your University Hall Management account. Use the 6-digit OTP code below to complete your verification:</div>"
                + "<div class='otp-card'>"
                + "<div class='otp-label'>Verification Code (OTP)</div>"
                + "<div class='otp-code'>" + otpCode + "</div>"
                + "<div class='otp-expiry'>⏳ This code is valid for 60 seconds.</div>"
                + "</div>"
                + "<div class='warning'>⚠️ <strong>Security Notice:</strong> Never share this code with anyone. RUET Hall administration will never ask for your verification code or password.</div>"
                + "</div>"
                + "<div class='footer'>"
                + "This is an automated email notification from RUET University Hall Management System.<br>"
                + "&copy; " + java.time.Year.now().getValue() + " Rajshahi University of Engineering &amp; Technology. All rights reserved."
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
}
