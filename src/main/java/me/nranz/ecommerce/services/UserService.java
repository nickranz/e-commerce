package me.nranz.ecommerce.services;

import me.nranz.ecommerce.dto.request.UpdateUserProfileRequest;
import me.nranz.ecommerce.dto.response.UpdateUserProfileResponse;
import me.nranz.ecommerce.entity.User;
import me.nranz.ecommerce.exceptions.UserAlreadyExistsException;
import me.nranz.ecommerce.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UpdateUserProfileResponse updateUserProfile(UpdateUserProfileRequest request, String username) {
        User user = userRepository.findByUsername(username).get();

        // Update only the fields from the request
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), user.getId())) {
                throw new UserAlreadyExistsException("Email is already in use.");
            }
            user.setEmail(request.getEmail());
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        // Save - JPA sees existing ID so it updates instead of inserts
        User saved = userRepository.save(user);

        return new UpdateUserProfileResponse(
                saved
        );

    }
}
