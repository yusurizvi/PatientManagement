package com.ps.authservice.service;

import com.ps.authservice.model.User;
//import com.ps.authservice.repository.UserRepository;
import com.ps.authservice.repository.RepositoryUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private RepositoryUser userRepository;

    public UserService(RepositoryUser userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
