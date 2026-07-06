package com.example.todoapi.controller;

import com.example.todoapi.dto.CreateTodoRequest;
import com.example.todoapi.dto.UpdateTodoRequest;
import com.example.todoapi.dto.UpdateTodoStatusRequest;
import com.example.todoapi.model.Todo;
import com.example.todoapi.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * GET /api/todos
     * Returns all todos.
     */
    @GetMapping
    public ResponseEntity<List<Todo>> getAllTodos() {
        return ResponseEntity.ok(todoService.getAllTodos());
    }

    /**
     * GET /api/todos/{id}
     * Returns one todo by id. test for feature branch
     *
     */
    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(@PathVariable Long id) {
        return ResponseEntity.ok(todoService.getTodoById(id));
    }

    /**
     * POST /api/todos
     * Creates a new todo.
     */
    @PostMapping
    public ResponseEntity<Todo> createTodo(@Valid @RequestBody CreateTodoRequest request) {
        Todo createdTodo = todoService.createTodo(request);

        // Build the Location header, e.g. /api/todos/1.
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTodo.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdTodo);
    }

    /**
     * PUT /api/todos/{id}
     * Fully updates a todo's editable fields.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTodoRequest request
    ) {
        return ResponseEntity.ok(todoService.updateTodo(id, request));
    }

    /**
     * PATCH /api/todos/{id}/status
     * Updates only the completed status.
     * This is not required for basic CRUD, but useful for learning PATCH.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Todo> updateTodoStatus(
            @PathVariable Long id,
            @RequestBody UpdateTodoStatusRequest request
    ) {
        return ResponseEntity.ok(todoService.updateTodoStatus(id, request.completed()));
    }

    /**
     * DELETE /api/todos/{id}
     * Deletes a todo and returns HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return ResponseEntity.noContent().build();
    }
}
