package com.codearena.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student extends User {

    private String year;
    private String branch;

    @Builder
    public Student(String name, String email, String passwordHash, Role role,
                   boolean emailVerified, boolean enabled, boolean approved,
                   String college, String avatarUrl, String otpCode,
                   java.time.Instant otpExpiresAt, String year, String branch) {
        super(name, email, passwordHash, role != null ? role : Role.STUDENT,
              emailVerified, enabled, approved, college, avatarUrl, otpCode, otpExpiresAt);
        this.year = year;
        this.branch = branch;
    }
}
