package com.example.taskapi.audit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for audit logs stored in MongoDB.
 * Only activated when MongoDB URI is configured.
 */
@Repository
@ConditionalOnProperty(name = "spring.data.mongodb.uri")
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);

    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    @Query("{ 'timestamp': { $gte: ?0 } }")
    List<AuditLog> findByTimestampAfter(Instant since);

    List<AuditLog> findByUsernameAndActionOrderByTimestampDesc(String username, String action);
}