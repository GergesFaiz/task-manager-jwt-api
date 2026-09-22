package com.example.taskapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    // Same base64 secret used by the test profile
    private static final String SECRET =
            "+I2JcrCMFHX4lPmA2PJUeuRtE9+R0uWmpuMb0BPH3pQEQsMNSbD1kCYf7EWYdrP1";

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        var secretField = JwtService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, SECRET);
        var expField = JwtService.class.getDeclaredField("expirationMs");
        expField.setAccessible(true);
        expField.set(jwtService, 86_400_000L);
    }

    private UserDetails user() {
        return User.withUsername("ahmed@example.com")
                .password("dummy")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    void generatesTokenContainingUsername() {
        String token = jwtService.generateToken(user());
        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("ahmed@example.com");
    }

    @Test
    void validatesOwnToken() {
        String token = jwtService.generateToken(user());
        assertThat(jwtService.isTokenValid(token, user())).isTrue();
    }

    @Test
    void rejectsTokenForDifferentUser() {
        String token = jwtService.generateToken(user());
        UserDetails other = User.withUsername("sara@example.com")
                .password("dummy")
                .authorities("ROLE_USER")
                .build();
        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }
}