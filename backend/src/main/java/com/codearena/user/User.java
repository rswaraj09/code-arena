package com.codearena.user;

import com.codearena.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    private String name;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private Role role;

    @Builder.Default
    private boolean emailVerified = false;

    @Builder.Default
    private boolean enabled = true;

    // Trainer accounts require admin approval before they can create contests/workshops.
    @Builder.Default
    private boolean approved = true;

    private String college;

    private String avatarUrl;

    private String otpCode;

    private java.time.Instant otpExpiresAt;
}
