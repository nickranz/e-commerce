package me.nranz.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.nranz.ecommerce.entity.User;

@Data
@AllArgsConstructor
public class UpdateUserProfileResponse {

    private User user;
}
