package com.example.LoginCompleto.support;

import com.example.LoginCompleto.user.dto.UserCreateDTO;
import com.example.LoginCompleto.user.entity.User;
import com.example.LoginCompleto.user.entity.enums.UserRole;
import com.example.LoginCompleto.user.service.UserService;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
public abstract class AbstractIntegrationTest {

    protected static final String DEFAULT_PASSWORD = "senha123";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected RecordingMailService mailService;

    @Autowired
    protected UserService userService;

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.getCollectionNames()
                .forEach(collection -> mongoTemplate.getCollection(collection).deleteMany(new Document()));
        mailService.clear();
    }

    protected User createUser(String username, UserRole role) {
        var dto = new UserCreateDTO();
        dto.setName("Usuário " + username);
        dto.setUsername(username);
        dto.setEmail(username + "@mail.com");
        dto.setPassword(DEFAULT_PASSWORD);
        dto.setConfirmPassword(DEFAULT_PASSWORD);
        dto.setRole(role);
        return userService.register(dto);
    }
}
