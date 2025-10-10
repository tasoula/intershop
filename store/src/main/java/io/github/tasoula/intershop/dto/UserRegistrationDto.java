package io.github.tasoula.intershop.dto;

import lombok.Data;

@Data
public class UserRegistrationDto {
    private String username;
    private String password;
    private boolean isAdmin;
}
