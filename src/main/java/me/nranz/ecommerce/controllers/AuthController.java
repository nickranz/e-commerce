package me.nranz.ecommerce.controllers;

import jakarta.validation.Valid;
import me.nranz.ecommerce.dto.request.LoginRequest;
import me.nranz.ecommerce.dto.request.RegisterRequest;
import me.nranz.ecommerce.dto.response.LoginResponse;
import me.nranz.ecommerce.dto.response.RegisterResponse;
import me.nranz.ecommerce.model.LoginCreds;
import me.nranz.ecommerce.services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerHandler(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginHandler(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
//        String token = authService.login(body.getUsername(), body.getPassword());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}