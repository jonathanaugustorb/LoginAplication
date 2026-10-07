package service;

import org.springframework.stereotype.Service;
import repository.IUserRepository;

@Service

public class UserService implements IUserRepository {

    private final IUserRepository userRepository;



}
