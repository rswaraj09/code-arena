package com.codearena.auth;

import com.codearena.auth.dto.*;
import com.codearena.common.exception.BadRequestException;
import com.codearena.common.exception.DuplicateResourceException;
import com.codearena.common.exception.ResourceNotFoundException;
import com.codearena.config.JwtProperties;
import com.codearena.security.JwtTokenProvider;
import com.codearena.user.Role;
import com.codearena.user.Student;
import com.codearena.user.StudentRepository;
import com.codearena.user.Trainer;
import com.codearena.user.TrainerRepository;
import com.codearena.user.User;
import com.codearena.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Duration OTP_VALIDITY = Duration.ofMinutes(10);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StudentRepository studentRepository;
    private final TrainerRepository trainerRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final EmailService emailService;
    private final UserService userService;

    public void register(RegisterRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }
        if (request.role() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be self-registered.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        if (request.role() == Role.STUDENT) {
            Student student = Student.builder()
                    .name(request.name())
                    .email(request.email())
                    .passwordHash(encodedPassword)
                    .role(Role.STUDENT)
                    .college(request.college())
                    .emailVerified(false)
                    .enabled(true)
                    .approved(true)
                    .build();
            studentRepository.save(student);
        } else if (request.role() == Role.TRAINER) {
            Trainer trainer = Trainer.builder()
                    .name(request.name())
                    .email(request.email())
                    .passwordHash(encodedPassword)
                    .role(Role.TRAINER)
                    .college(request.college())
                    .emailVerified(false)
                    .enabled(true)
                    .approved(true)
                    .build();
            trainerRepository.save(trainer);
        }

        issueOtp(request.email(), "Email Verification Code");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userService.getByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        if (!user.isEnabled()) {
            throw new BadRequestException("Your account has been disabled. Please contact support.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        return issueTokenPair(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token."));

        if (stored.isRevoked() || stored.isExpired()) {
            throw new BadRequestException("Refresh token expired or revoked. Please sign in again.");
        }

        User user = userService.getById(stored.getUserId());
        stored.setRevoked(true); // rotate on every use
        refreshTokenRepository.save(stored);

        return issueTokenPair(user);
    }

    public void logout(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public void verifyOtp(VerifyOtpRequest request) {
        User user = userService.getByEmail(request.email());
        validateOtp(user, request.code());

        user.setEmailVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        userService.save(user);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        if (userService.existsByEmail(request.email())) {
            issueOtp(request.email(), "Password Reset Code");
        }
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userService.getByEmail(request.email());
        validateOtp(user, request.code());

        String encodedPassword = passwordEncoder.encode(request.newPassword());
        user.setPasswordHash(encodedPassword);
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        userService.save(user);

        refreshTokenRepository.deleteByUserId(user.getId());
    }

    // ---- helpers ----

    private AuthResponse issueTokenPair(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .userId(user.getId())
                .expiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpiryMs()))
                .build();
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshTokenValue, userService.toResponse(user));
    }

    private void issueOtp(String email, String purposeTitle) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        Instant expiresAt = Instant.now().plus(OTP_VALIDITY);

        User user = userService.getByEmail(email);
        user.setOtpCode(code);
        user.setOtpExpiresAt(expiresAt);
        userService.save(user);

        emailService.sendOtp(email, code, purposeTitle);
    }

    private void validateOtp(User user, String suppliedCode) {
        if (user.getOtpCode() == null || user.getOtpExpiresAt() == null) {
            throw new ResourceNotFoundException("No pending verification code found for this account.");
        }
        if (user.getOtpExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("This code has expired. Please request a new one.");
        }
        if (!user.getOtpCode().equals(suppliedCode)) {
            throw new BadRequestException("Incorrect verification code.");
        }
    }
}
