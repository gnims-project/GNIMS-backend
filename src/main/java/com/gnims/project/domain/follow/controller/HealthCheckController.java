package com.gnims.project.domain.follow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping
    public String healthCheck() {

        return "gnims ok";
    }
}
