package com.college.crypto.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/ping")
    public String home() {
        return "Crypto Trading Platform Backend is RUNNING 🚀";
    }
}
