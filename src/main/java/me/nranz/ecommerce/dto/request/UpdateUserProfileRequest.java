package me.nranz.ecommerce.dto.request;


import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserProfileRequest {

    private String firstName;

    private String lastName;

    @Email(message = "Email must be valid")
    private String email;
}
