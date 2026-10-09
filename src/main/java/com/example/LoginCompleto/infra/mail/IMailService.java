package com.example.LoginCompleto.infra.mail;

import java.util.Map;

public interface IMailService {

    void send(String to, String subject, String template, Map<String, Object> variables);
}
