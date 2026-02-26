package me.nranz.ecommerce.services;

import me.nranz.ecommerce.dto.request.LoginRequest;
import me.nranz.ecommerce.dto.request.RegisterRequest;
import me.nranz.ecommerce.dto.response.LoginResponse;
import me.nranz.ecommerce.dto.response.RegisterResponse;
import me.nranz.ecommerce.entity.User;
import me.nranz.ecommerce.exceptions.InvalidCredentialsException;
import me.nranz.ecommerce.exceptions.UserAlreadyExistsException;
import me.nranz.ecommerce.repositories.UserRepository;
import me.nranz.ecommerce.security.JwtUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            JwtUtil jwtUtil,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse register(RegisterRequest request) {
        // Check if username has been created before
        if (userRepository.existsByUsernameOrEmail(request.getUsername(), request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists");
        }

        // Convert DTO -> Entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save Entity
        User saved = null;
        try {
            saved = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        // Generate token
        String token = jwtUtil.generateToken(saved.getUsername());

        // Convert Entity → Response DTO
        return new RegisterResponse(
                token,
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getRole()
        );

    }

    public LoginResponse login(LoginRequest request) {
        try {
            String username = request.getUsername();
            String password = request.getPassword();

            // Authenticate login creds
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            // If we get here, we've authenticated, create their bearer token and grab login response details.
            String token = jwtUtil.generateToken(username);

            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));


            return new LoginResponse(token, user);

        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }
    }

    public String login(String username, String password) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(username, password);
            authenticationManager.authenticate(authToken);
            return jwtUtil.generateToken(username);
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid username or password.");
        }
    }
}