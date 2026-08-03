package com.codearena.user;

import com.codearena.common.exception.ResourceNotFoundException;
import com.codearena.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final StudentRepository studentRepository;
    private final TrainerRepository trainerRepository;

    public User getById(String id) {
        Optional<Student> student = studentRepository.findById(id);
        if (student.isPresent()) return student.get();

        Optional<Trainer> trainer = trainerRepository.findById(id);
        if (trainer.isPresent()) return trainer.get();

        throw ResourceNotFoundException.of("User", "id", id);
    }

    public User getByEmail(String email) {
        Optional<Student> student = studentRepository.findByEmail(email);
        if (student.isPresent()) return student.get();

        Optional<Trainer> trainer = trainerRepository.findByEmail(email);
        if (trainer.isPresent()) return trainer.get();

        throw ResourceNotFoundException.of("User", "email", email);
    }

    public boolean existsByEmail(String email) {
        return studentRepository.existsByEmail(email)
                || trainerRepository.existsByEmail(email);
    }

    public void save(User user) {
        if (user instanceof Student s) {
            studentRepository.save(s);
        } else if (user instanceof Trainer t) {
            trainerRepository.save(t);
        }
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
