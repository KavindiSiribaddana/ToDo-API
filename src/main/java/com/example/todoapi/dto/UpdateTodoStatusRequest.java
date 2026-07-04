package com.example.todoapi.dto;

/**
 * Small DTO used for updating only the completed/not completed status.
 */
public record UpdateTodoStatusRequest(boolean completed) {
}
