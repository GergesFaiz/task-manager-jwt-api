package com.example.taskapi.auth;

import com.example.taskapi.audit.AuditService;
import com.example.taskapi.auth.dto.AuthResponse;
import com.example.taskapi.auth.dto.LoginRequest;
import com.example.taskapi.auth.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Autowired(required = false)
    private AuditService auditService; // optional - only available when MongoDB configured

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                  HttpServletRequest httpRequest) {
        AuthResponse response = authService.register(request);
        if (auditService != null) {
            auditService.logSuccess(request.email(), "REGISTER", "USER", response.email(),
                    httpRequest, "User registered successfully");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(request);
        if (auditService != null) {
            auditService.logSuccess(request.email(), "LOGIN", "USER", response.email(),
                    httpRequest, "User logged in successfully");
        }
        return ResponseEntity.ok(response);
    }
}