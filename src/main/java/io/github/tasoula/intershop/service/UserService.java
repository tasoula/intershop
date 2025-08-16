package io.github.tasoula.intershop.service;

import io.github.tasoula.intershop.dao.UserRepository;
import io.github.tasoula.intershop.model.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UUID createUser() {
        User savedUser = repository.save(new User());
        return savedUser.getId();
    }
}
