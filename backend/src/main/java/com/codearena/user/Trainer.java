package com.codearena.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "trainers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trainer extends User {

    private String organization;
    private String bio;
    private String specialization;

    @Builder
    public Trainer(String name, String email, String passwordHash, Role role,
                   boolean emailVerified, boolean enabled, boolean approved,
                   String college, String avatarUrl, String otpCode,
                   java.time.Instant otpExpiresAt, String organization,
                   String bio, String specialization) {
        super(name, email, passwordHash, role != null ? role : Role.TRAINER,
              emailVerified, enabled, approved, college, avatarUrl, otpCode, otpExpiresAt);
        this.organization = organization;
        this.bio = bio;
        this.specialization = specialization;
    }
}
