package me.nranz.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RegisterResponse {

    private String token;
    private final String type = "Bearer";
    private UUID id;
    private String username;
    private String email;
    private String role;
}
