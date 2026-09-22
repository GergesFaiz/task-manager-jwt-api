package com.example.taskapi.auth;

import com.example.taskapi.auth.dto.AuthResponse;
import com.example.taskapi.auth.dto.LoginRequest;
import com.example.taskapi.auth.dto.RegisterRequest;
import com.example.taskapi.security.JwtService;
import com.example.taskapi.user.Role;
import com.example.taskapi.user.User;
import com.example.taskapi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new BadCredentialsException("This email is already registered");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        userRepository.save(user);

        return buildAuthResponse(email);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase().trim();

        // Throws BadCredentialsException if the login is wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));

        return buildAuthResponse(email);
    }

    private AuthResponse buildAuthResponse(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        User user = userRepository.findByEmail(email).orElseThrow();
        return new AuthResponse(jwtService.generateToken(userDetails), user.getEmail(), user.getFullName());
    }
}