package com.chakri.todo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TodoController {

    @GetMapping("/")
    public String home() {
        return "Git Team Todo App - Jenkins CI/CD is working!";
    }

    @GetMapping("/health")
    public String health() {
        return "UP";
    }
}
