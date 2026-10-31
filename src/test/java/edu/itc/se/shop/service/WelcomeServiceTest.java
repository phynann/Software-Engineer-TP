package edu.itc.se.shop.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WelcomeServiceTest {
    static class FakeNotifier implements Notifier {
        final List<String> sent = new ArrayList<>();
        @Override
        public void send(String to, String text) {
            sent.add(to + ":" + text);
        }
    }

    @Test
    void welcomeUsesAppropriateNotifierAndFixedClock() {
        var defaultNotif = new FakeNotifier();
        var urgentNotif = new FakeNotifier();
        var clock = Clock.fixed(Instant.parse("2026-10-31T09:15:02Z"), ZoneId.of("UTC"));
        var service = new WelcomeService(defaultNotif, urgentNotif, clock);

        String msg1 = service.welcome("ann", false);
        assertEquals("Welcome ann! It is 09:15:02", msg1);
        assertEquals(1, defaultNotif.sent.size());
        assertEquals(0, urgentNotif.sent.size());

        String msg2 = service.welcome("bob", true);
        assertEquals("Welcome bob! It is 09:15:02", msg2);
        assertEquals(1, urgentNotif.sent.size());
    }
}
