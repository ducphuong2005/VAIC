package com.careercompass.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.careercompass.config.JwtProperties;
import com.careercompass.entity.User;
import com.careercompass.entity.UserRole;
import com.careercompass.entity.UserStatus;
import com.careercompass.service.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(new JwtProperties(
            "test-secret-for-jwt-authentication-minimum-32-bytes",
            30,
            14
    ));

    @Test
    void validatesAccessTokenForMatchingUser() {
        User user = testUser();
        String token = jwtService.generateAccessToken(user);
        UserPrincipal principal = UserPrincipal.from(user);

        assertThat(jwtService.extractAccessUserId(token)).contains(user.getId());
        assertThat(jwtService.isAccessTokenValid(token, principal)).isTrue();
    }

    @Test
    void rejectsRefreshTokenAsAccessToken() {
        User user = testUser();
        String token = jwtService.generateRefreshToken(user);

        assertThat(jwtService.extractAccessUserId(token)).isEmpty();
    }

    private User testUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("student@example.com")
                .passwordHash("hash")
                .fullName("Student")
                .role(UserRole.STUDENT)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
