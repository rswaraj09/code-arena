package com.codearena.user;

import com.codearena.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class User extends BaseEntity {

    private String name;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private Role role;

    private boolean emailVerified;

    private boolean enabled;

    // Trainer accounts require admin approval before they can create contests/workshops.
    private boolean approved;

    private String college;

    private String avatarUrl;

    private String otpCode;

    private java.time.Instant otpExpiresAt;
}
