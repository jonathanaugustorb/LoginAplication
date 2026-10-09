package com.example.LoginCompleto.support;

import com.example.LoginCompleto.infra.mail.IMailService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class RecordingMailService implements IMailService {

    private final List<SentMail> sentMails = new CopyOnWriteArrayList<>();

    @Override
    public void send(String to, String subject, String template, Map<String, Object> variables) {
        sentMails.add(new SentMail(to, subject, template, Map.copyOf(variables)));
    }

    public List<SentMail> sentMails() {
        return List.copyOf(sentMails);
    }

    public void clear() {
        sentMails.clear();
    }

    public record SentMail(String to, String subject, String template, Map<String, Object> variables) {
    }
}
