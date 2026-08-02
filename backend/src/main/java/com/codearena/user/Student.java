package com.codearena.user;

import com.codearena.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseEntity {

    private String name;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    @Builder.Default
    private Role role = Role.STUDENT;

    @Builder.Default
    private boolean emailVerified = false;

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private boolean approved = true;

    private String college;

    private String avatarUrl;

    private String year;
    private String branch;

    private String otpCode;
    private java.time.Instant otpExpiresAt;
}
