package com.codearena.auth.dto;

import com.codearena.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 120) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$",
                message = "Password must be at least 8 characters and include a number and a symbol."
        )
        String password,
        @NotNull Role role,
        String college
) {
}
