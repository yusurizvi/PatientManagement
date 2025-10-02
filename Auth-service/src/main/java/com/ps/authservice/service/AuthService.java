package com.ps.authservice.service;

import com.ps.authservice.dto.LoginRequest;
import com.ps.authservice.model.User;
//import com.ps.authservice.repository.UserService;
import com.ps.authservice.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {

        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Optional<String> authenticate(LoginRequest loginReq){

        return userService.findByEmail(loginReq.getEmail())
                .filter(user -> passwordEncoder.matches(loginReq.getPassword(),user.getPassword()))
                .map(user -> jwtUtil.getToken(user.getEmail(),user.getRole()));
    }


}
