package com.codearena.auth;

import com.codearena.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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

    private String userId;

    private Instant expiresAt;

    @Builder.Default
    private boolean revoked = false;

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
}
