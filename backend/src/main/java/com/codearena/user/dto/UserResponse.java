package com.codearena.user.dto;

import com.codearena.user.Role;

public record UserResponse(
        String id,
        String name,
        String email,
        Role role,
        String college,
        String avatarUrl,
        boolean emailVerified,
        boolean approved
) {
}
