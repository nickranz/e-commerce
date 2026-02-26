package me.nranz.ecommerce.controllers;

import me.nranz.ecommerce.dto.request.UpdateUserProfileRequest;
import me.nranz.ecommerce.dto.response.LoginResponse;
import me.nranz.ecommerce.dto.response.UpdateUserProfileResponse;
import me.nranz.ecommerce.entity.User;
import me.nranz.ecommerce.repositories.UserRepository;
import me.nranz.ecommerce.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public User getUserDetails() {
        String userName = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userRepository.findByUsername(userName).get();
    }

    @PutMapping("/me")
    public ResponseEntity<UpdateUserProfileResponse> updateProfile(@RequestBody UpdateUserProfileRequest request) {

        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UpdateUserProfileResponse response = userService.updateUserProfile(request, username);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
