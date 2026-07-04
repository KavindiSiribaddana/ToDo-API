package com.example.todoapi.repository;

import com.example.todoapi.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaRepository gives us basic database methods automatically:
 * save(), findAll(), findById(), deleteById(), existsById(), etc.
 */
public interface TodoRepository extends JpaRepository<Todo, Long> {
}
