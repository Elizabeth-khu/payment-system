package com.innowise.orderservice.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String id) {
        super("Item not found with id: " + id);
    }
}