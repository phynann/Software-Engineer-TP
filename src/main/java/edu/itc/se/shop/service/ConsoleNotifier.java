package edu.itc.se.shop.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Primary
@Profile("dev")
public class ConsoleNotifier implements Notifier {

    @PostConstruct
    public void init() {
        System.out.println("ConsoleNotifier ready");
    }

    @Override
    public void send(String to, String text) {
        System.out.println("CONSOLE to=" + to + " text=" + text);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("ConsoleNotifier closing");
    }
}
