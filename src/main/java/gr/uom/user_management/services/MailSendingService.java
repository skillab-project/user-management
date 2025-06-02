package gr.uom.user_management.services;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class MailSendingService {

    JavaMailSender mailSender;

    public MailSendingService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private String loadHtmlTemplate() {
        try {
            //System.getProperty("user.dir") + "\\src\\main\\resources\\emailTemplates" + templateName
            Resource resource = new ClassPathResource("emailTemplates/template-email-basic.html");
            byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
            return new String(bdata, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error loading template
            return "";
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        try {
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // Set HTML content to true
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendPasswordResetEmail(String toEmail, String passResetCode, UUID uuid, String frontendUrl) {
        String subject = "Skillab | Password Reset Request";
        String resetUrl = frontendUrl + "/reset-password?token=" + passResetCode + "&uuid=" + uuid;

        // Load HTML template from file
        String htmlContent = loadHtmlTemplate();

        // Replace placeholders in the template with actual values
        htmlContent = htmlContent.replace("{{GREETING_TEXT}}", "Hello!");
        htmlContent = htmlContent.replace("{{GREETING_SUBTEXT}}", "You have requested a password reset for your Skillab account.");
        htmlContent = htmlContent.replace("{{MAIL_BODY_TEXT}}", "To reset your password, please click the button below.");
        htmlContent = htmlContent.replace("{{BUTTON_SECTION_HEADER_TEXT}}", "Click the button to reset your password");
        htmlContent = htmlContent.replace("{{BUTTON_SECTION_URL}}", resetUrl);
        htmlContent = htmlContent.replace("{{BUTTON_TEXT}}", "Reset Password");
        htmlContent = htmlContent.replace("{{BUTTON_SUB_TEXT}}", "If the button does not work, please copy and paste the following link in your browser: ");
        htmlContent = htmlContent.replace("{{BUTTON_SUB_TEXT_CAPTION}}", resetUrl);

        // Send email with HTML content
        sendHtmlEmail(toEmail, subject, htmlContent);
    }
}
