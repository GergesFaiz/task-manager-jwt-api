package com.example.taskapi.task;

import com.example.taskapi.exception.NotFoundException;
import com.example.taskapi.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
                                              @AuthenticationPrincipal User currentUser) {
        Task task = new Task();
        task.setTitle(dto.title().trim());
        task.setDescription(dto.description());
        task.setCompleted(dto.completed() != null && dto.completed());
        task.setOwner(currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(taskRepository.save(task)));
    }

    @PutMapping("/{id}")
    public TaskDto updateTask(@PathVariable Long id,
                              @Valid @RequestBody TaskDto dto,
                              @AuthenticationPrincipal User currentUser) {
        Task task = findOwnedTask(id, currentUser);
        task.setTitle(dto.title().trim());
        task.setDescription(dto.description());
        if (dto.completed() != null) {
            task.setCompleted(dto.completed());
        }
        return toDto(taskRepository.save(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Task task = findOwnedTask(id, currentUser);
        taskRepository.delete(task);
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