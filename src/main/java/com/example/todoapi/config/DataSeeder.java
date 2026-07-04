package com.example.todoapi.config;

import com.example.todoapi.model.Todo;
import com.example.todoapi.repository.TodoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Adds a few records when the application starts.
 * This makes GET /api/todos return sample data immediately.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final TodoRepository todoRepository;

    public DataSeeder(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Override
    public void run(String... args) {
        if (todoRepository.count() == 0) {
            todoRepository.save(new Todo("Learn Spring Boot", "Understand controllers, services, and repositories"));
            todoRepository.save(new Todo("Test API using Postman", "Invoke CRUD endpoints locally"));
        }
    }
}
