package org.example.mail.service.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mail.service.config.ConfigLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final JavaMailSender mailSender;
    private final ConfigLoader configLoader;

    public void sendEmail(String to, String subject, String text, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, isHtml);
            helper.setFrom(configLoader.getUsername());

            mailSender.send(message);
            log.info("\uD83D\uDCE7 Email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage());
        }
    }
}
