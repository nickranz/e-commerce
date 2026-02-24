package me.nranz.ecommerce.controllers;

import me.nranz.ecommerce.entity.User;
import me.nranz.ecommerce.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/info")
    public User getUserDetails() {
        String userName = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userRepository.findByUsername(userName).get();
    }
}
