package com.ps.authservice.controller;

import com.ps.authservice.dto.LoginRequest;
import com.ps.authservice.dto.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.text.html.Option;
import java.util.Optional;

@RestController
@RequestMapping("/login")
public class LoginController {

    public ResponseEntity<String> login(LoginRequest loginRequest) {

        Optional<String> resp = authService.authenticate(loginRequest);
        if(resp.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String token = resp.get();
        return ResponseEntity.ok(token);
    }
}
