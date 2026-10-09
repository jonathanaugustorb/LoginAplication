package com.example.LoginCompleto.user.service;

import com.example.LoginCompleto.common.exception.BusinessException;
import com.example.LoginCompleto.common.service.AbstractCrudService;
import com.example.LoginCompleto.user.dto.UserCreateDTO;
import com.example.LoginCompleto.user.entity.User;
import com.example.LoginCompleto.user.mapper.UserMapper;
import com.example.LoginCompleto.user.repository.IUserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService extends AbstractCrudService<User, IUserRepository> {

    private static final String DUPLICATED_USER_MESSAGE = "Nome de usuário ou e-mail já cadastrado";

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(IUserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        super(userRepository, "Usuário");
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public User register(UserCreateDTO dto) {
        User user = userMapper.toEntity(dto);

        if (repository.existsByUsername(user.getUsername())) {
            throw new BusinessException("Nome de usuário já está em uso");
        }
        if (repository.existsByEmail(user.getEmail())) {
            throw new BusinessException("E-mail já está em uso");
        }

        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        return saveUnique(user);
    }

    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = findById(userId);

        if (!passwordMatches(user, currentPassword)) {
            throw new BusinessException("Senha atual incorreta");
        }
        if (passwordMatches(user, newPassword)) {
            throw new BusinessException("A nova senha deve ser diferente da atual");
        }

        updatePassword(user, newPassword);
    }

    public void updatePassword(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        repository.save(user);
    }

    public boolean passwordMatches(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(userMapper.normalize(username));
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(userMapper.normalize(email));
    }

    private User saveUnique(User user) {
        try {
            return repository.save(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(DUPLICATED_USER_MESSAGE);
        }
    }
}
