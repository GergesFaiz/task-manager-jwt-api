package com.example.taskapi.auth.dto;

public record AuthResponse(
        String token,
        String email,
        String fullName
) {}