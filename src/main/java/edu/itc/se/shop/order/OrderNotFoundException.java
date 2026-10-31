package edu.itc.se.shop.order;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(long id) {
        super("Order " + id + " not found");
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}
