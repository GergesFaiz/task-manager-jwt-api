package com.example.taskapi.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskDto(
        Long id,
        @NotBlank(message = "Title is required")
        @Size(max = 200)
        String title,
        @Size(max = 1000)
        String description,
        Boolean completed,
        java.time.Instant createdAt
) {}