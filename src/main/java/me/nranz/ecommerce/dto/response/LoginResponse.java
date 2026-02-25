package me.nranz.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.nranz.ecommerce.entity.User;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private final String type = "Bearer";
    //    private Long id;
//    private String username;
//    private String email;
//    private String role;
    private User user;
}
