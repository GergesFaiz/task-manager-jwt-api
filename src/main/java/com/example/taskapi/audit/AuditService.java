package com.example.taskapi.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

/**
 * Service for recording audit events to MongoDB.
 * Bean is created conditionally via AuditConfig when MongoDB is configured.
 */
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void logSuccess(String username, String action, String resourceType, String resourceId,
                           HttpServletRequest request, String details) {
        AuditLog log = AuditLog.builder()
                .username(username)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .details(details)
                .timestamp(Instant.now())
                .success(true)
                .build();
        auditLogRepository.save(log);
    }

    public void logFailure(String username, String action, String resourceType, String resourceId,
                           HttpServletRequest request, String details, String errorMessage) {
        AuditLog log = AuditLog.builder()
                .username(username)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .details(details)
                .timestamp(Instant.now())
                .success(false)
                .errorMessage(errorMessage)
                .build();
        auditLogRepository.save(log);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}