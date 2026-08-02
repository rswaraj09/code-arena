package com.codearena.auth;

import com.codearena.common.BaseEntity;
import com.codearena.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.Instant;

@Document(collection = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

    @Indexed(unique = true)
    private String token;

    @DocumentReference
    private User user;

    private Instant expiresAt;

    @Builder.Default
    private boolean revoked = false;

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
}
