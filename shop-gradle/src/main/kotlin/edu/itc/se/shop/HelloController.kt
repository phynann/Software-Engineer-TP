package edu.itc.se.shop

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class Greeting(
    val message: String,
    val at: Instant
)

@RestController
class HelloController {
    @GetMapping("/hello")
    fun hello(
        @RequestParam(defaultValue = "World") name: String
    ): Greeting {
        return Greeting("Hello, $name!", Instant.now())
    }
}
