package com.example.todoapi.exception;

/**
 * Custom exception used when a todo id does not exist.
 */
public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(Long id) {
        super("Todo not found with id: " + id);
    }
}
