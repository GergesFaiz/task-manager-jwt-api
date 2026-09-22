package com.example.taskapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TaskApiApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context starts correctly (H2 in-memory).
    }
}