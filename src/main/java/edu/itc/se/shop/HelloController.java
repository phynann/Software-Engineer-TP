package edu.itc.se.shop;

import java.time.Instant;
import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {

    public record Greeting(String message, Instant at) {}

    @GetMapping("/hello")
    public Greeting hello(@RequestParam(defaultValue = "World") String name) {
        // TODO: return a Greeting
        return new Greeting("Hello "+ name + "!", Instant.now());
    }
}