package org.example.videoviewer.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
public class Mailer {
    private final JavaMailSender mailSender;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public void sendSimpleMail(final String to, final String subject, final String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        executorService.submit(() -> {
            mailSender.send(message);
        });
    }

    public void sendHtmlMail(final String to, final String subject, final String text) throws MessagingException, FileNotFoundException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        messageHelper.setText(text, true);

        executorService.submit(() -> {
            mailSender.send(mimeMessage);
        });
    }

    public void sendMailWithAttachment(final String to, final String subject, final String text, final File file) throws MessagingException, FileNotFoundException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        messageHelper.setText(text, true);
        messageHelper.addAttachment("Cat.jpg", file);

        executorService.submit(() -> {
            mailSender.send(mimeMessage);
        });
    }
}
