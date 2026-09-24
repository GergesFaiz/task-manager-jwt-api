package com.example.taskapi.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

/**
 * Audit log entry stored in MongoDB.
 * Captures security-relevant events for compliance and debugging.
 */
@Document(collection = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    private String id;

    @Indexed
    private String username; // email of the user who performed the action

    @Indexed
    private String action; // e.g., "LOGIN", "LOGOUT", "TASK_CREATE", "TASK_UPDATE", "TASK_DELETE", "REGISTER"

    private String resourceType; // e.g., "TASK", "USER", "AUTH"
    private String resourceId;   // ID of the affected resource (task ID, user ID, etc.)

    private String ipAddress;
    private String userAgent;
    private String details; // JSON or free-text details

    private Instant timestamp;

    private boolean success;
    private String errorMessage; // populated if success=false
}