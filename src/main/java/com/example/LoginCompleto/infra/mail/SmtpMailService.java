package com.example.LoginCompleto.infra.mail;

import com.example.LoginCompleto.infra.config.AppProperties;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpMailService implements IMailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    @Async
    @Override
    public void send(String to, String subject, String template, Map<String, Object> variables) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

            helper.setFrom(appProperties.mail().from());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(templateEngine.process(template, new Context(null, variables)), true);

            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            log.error("Falha ao enviar e-mail '{}'", subject, exception);
        }
    }
}
