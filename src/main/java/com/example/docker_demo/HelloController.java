package com.example.docker_demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello-broken")
    public String hello() {
        return "Toi da deploy tu dong bang Jenkins!";
    }
}