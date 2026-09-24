package com.example.taskapi.task;

import com.example.taskapi.audit.AuditService;
import com.example.taskapi.exception.NotFoundException;
import com.example.taskapi.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskRepository taskRepository;

    @Autowired(required = false)
    private AuditService auditService; // optional - only available when MongoDB configured

    @GetMapping
    public List<TaskDto> listMyTasks(@AuthenticationPrincipal User currentUser) {
        return taskRepository.findByOwnerIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(TaskController::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public TaskDto getTask(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Task task = findOwnedTask(id, currentUser);
        return toDto(task);
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto dto,
                                              @AuthenticationPrincipal User currentUser,
                                              HttpServletRequest httpRequest) {
        Task task = new Task();
        task.setTitle(dto.title().trim());
        task.setDescription(dto.description());
        task.setCompleted(dto.completed() != null && dto.completed());
        task.setOwner(currentUser);

        Task saved = taskRepository.save(task);
        if (auditService != null) {
            auditService.logSuccess(currentUser.getEmail(), "TASK_CREATE", "TASK", saved.getId().toString(),
                    httpRequest, "Task created: " + saved.getTitle());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public TaskDto updateTask(@PathVariable Long id,
                              @Valid @RequestBody TaskDto dto,
                              @AuthenticationPrincipal User currentUser,
                              HttpServletRequest httpRequest) {
        Task task = findOwnedTask(id, currentUser);
        task.setTitle(dto.title().trim());
        task.setDescription(dto.description());
        if (dto.completed() != null) {
            task.setCompleted(dto.completed());
        }
        Task saved = taskRepository.save(task);
        if (auditService != null) {
            auditService.logSuccess(currentUser.getEmail(), "TASK_UPDATE", "TASK", task.getId().toString(),
                    httpRequest, "Task updated: " + saved.getTitle());
        }
        return toDto(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id,
                                           @AuthenticationPrincipal User currentUser,
                                           HttpServletRequest httpRequest) {
        Task task = findOwnedTask(id, currentUser);
        taskRepository.delete(task);
        if (auditService != null) {
            auditService.logSuccess(currentUser.getEmail(), "TASK_DELETE", "TASK", id.toString(),
                    httpRequest, "Task deleted: " + task.getTitle());
        }
        return ResponseEntity.noContent().build();
    }

    private Task findOwnedTask(Long id, User currentUser) {
        return taskRepository.findByIdAndOwnerId(id, currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + id));
    }

    private static TaskDto toDto(Task task) {
        return new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getCreatedAt());
    }
}