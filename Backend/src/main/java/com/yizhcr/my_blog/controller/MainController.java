package com.yizhcr.my_blog.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @GetMapping("/")
    public String home() {
        return "Welcome to My Blog API! Please visit /api to access blog features.";
    }
}