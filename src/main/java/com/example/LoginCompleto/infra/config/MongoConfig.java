package com.example.LoginCompleto.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing(auditorAwareRef = "securityAuditorAware")
public class MongoConfig {
}
