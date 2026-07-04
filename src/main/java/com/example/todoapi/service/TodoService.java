package com.example.todoapi.service;

import com.example.todoapi.dto.CreateTodoRequest;
import com.example.todoapi.dto.UpdateTodoRequest;
import com.example.todoapi.exception.TodoNotFoundException;
import com.example.todoapi.model.Todo;
import com.example.todoapi.repository.TodoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    // Constructor injection is preferred because it makes dependencies explicit and testable.
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public Todo getTodoById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }

    public Todo createTodo(CreateTodoRequest request) {
        Todo todo = new Todo(request.title(), request.description());
        return todoRepository.save(todo);
    }

    public Todo updateTodo(Long id, UpdateTodoRequest request) {
        // First fetch the existing todo. If not found, throw a 404-friendly exception.
        Todo existingTodo = getTodoById(id);

        // Update only fields that users are allowed to modify.
        existingTodo.setTitle(request.title());
        existingTodo.setDescription(request.description());
        existingTodo.setCompleted(request.completed());

        return todoRepository.save(existingTodo);
    }

    public Todo updateTodoStatus(Long id, boolean completed) {
        Todo existingTodo = getTodoById(id);
        existingTodo.setCompleted(completed);
        return todoRepository.save(existingTodo);
    }

    public void deleteTodo(Long id) {
        Todo existingTodo = getTodoById(id);
        todoRepository.delete(existingTodo);
    }
}
