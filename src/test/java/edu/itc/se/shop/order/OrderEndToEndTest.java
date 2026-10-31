package edu.itc.se.shop.order;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import edu.itc.se.shop.repository.ProductRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderEndToEndTest {

    @LocalServerPort
    private int port;

    private HttpClient client;

    @Autowired
    private ProductRepository productRepo;

    @BeforeEach
    void setup() {
        this.client = HttpClient.newHttpClient();
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void placeOrderDecrementsStockRollsBackOnFailureAndRestoresOnCancel() throws Exception {
        int initialStock1 = productRepo.findById(1L).orElseThrow().stock();
        int initialStock2 = productRepo.findById(2L).orElseThrow().stock();

        // 1. Place order successfully
        String orderBody = """
                {
                  "customerId": 1,
                  "lines": [
                    {"productId": 1, "qty": 2},
                    {"productId": 2, "qty": 1}
                  ]
                }
                """;

        HttpRequest placeReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/orders")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(orderBody))
                .build();

        HttpResponse<String> placeRes = client.send(placeReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, placeRes.statusCode());
        assertTrue(placeRes.headers().firstValue("Location").isPresent());
        String location = placeRes.headers().firstValue("Location").get();
        assertTrue(placeRes.body().contains("\"status\":\"NEW\""));

        // Check stock decremented
        int stockAfter1 = productRepo.findById(1L).orElseThrow().stock();
        int stockAfter2 = productRepo.findById(2L).orElseThrow().stock();
        assertEquals(initialStock1 - 2, stockAfter1);
        assertEquals(initialStock2 - 1, stockAfter2);

        // 2. Try placing order with insufficient stock -> must fail with 409 and roll back atomically
        String invalidOrderBody = """
                {
                  "customerId": 1,
                  "lines": [
                    {"productId": 1, "qty": 2},
                    {"productId": 2, "qty": 9999}
                  ]
                }
                """;

        HttpRequest conflictReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/orders")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(invalidOrderBody))
                .build();

        HttpResponse<String> conflictRes = client.send(conflictReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(409, conflictRes.statusCode());

        // Assert atomic rollback: stock remains untouched after 409 failure
        assertEquals(stockAfter1, productRepo.findById(1L).orElseThrow().stock());
        assertEquals(stockAfter2, productRepo.findById(2L).orElseThrow().stock());

        // 3. Cancel the original order -> stock must be restored
        HttpRequest cancelReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl(location + "/cancel")))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> cancelRes = client.send(cancelReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, cancelRes.statusCode());
        assertTrue(cancelRes.body().contains("\"status\":\"CANCELLED\""));

        // Check stock restored to initial levels
        assertEquals(initialStock1, productRepo.findById(1L).orElseThrow().stock());
        assertEquals(initialStock2, productRepo.findById(2L).orElseThrow().stock());
    }
}
