package com.codearena.user;

import com.codearena.common.exception.ResourceNotFoundException;
import com.codearena.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "id", id));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ResourceNotFoundException.of("User", "email", email));
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCollege(),
                user.getAvatarUrl(),
                user.isEmailVerified(),
                user.isApproved()
        );
    }
}
