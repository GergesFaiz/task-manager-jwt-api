package com.example.taskapi.config;

import com.example.taskapi.audit.AuditLogRepository;
import com.example.taskapi.audit.AuditService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conditionally creates AuditService bean only when MongoDB is explicitly enabled.
 * Set spring.data.mongodb.enabled=true in production profiles to activate.
 * This prevents the bean from being instantiated during tests when MongoDB is not available.
 */
@Configuration
@ConditionalOnProperty(name = "spring.data.mongodb.enabled", havingValue = "true", matchIfMissing = false)
public class AuditConfig {

    @Bean
    public AuditService auditService(AuditLogRepository auditLogRepository) {
        return new AuditService(auditLogRepository);
    }
}