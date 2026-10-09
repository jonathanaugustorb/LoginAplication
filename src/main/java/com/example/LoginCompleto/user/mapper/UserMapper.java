package com.example.LoginCompleto.user.mapper;

import com.example.LoginCompleto.user.dto.UserCreateDTO;
import com.example.LoginCompleto.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class UserMapper {

    public User toEntity(UserCreateDTO dto) {
        return User.builder()
                .name(dto.getName().trim())
                .username(normalize(dto.getUsername()))
                .email(normalize(dto.getEmail()))
                .role(dto.getRole())
                .build();
    }

    public String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
