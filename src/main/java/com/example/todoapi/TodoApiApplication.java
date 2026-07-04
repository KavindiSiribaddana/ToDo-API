package com.example.todoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TodoApiApplication {

    public static void main(String[] args) {
        // This starts the embedded Tomcat server and exposes the REST API.
        SpringApplication.run(TodoApiApplication.class, args);
    }
}
