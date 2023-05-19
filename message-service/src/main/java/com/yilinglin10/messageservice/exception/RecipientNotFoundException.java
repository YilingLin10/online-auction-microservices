package com.yilinglin10.messageservice.exception;

public class RecipientNotFoundException extends RuntimeException {
    public RecipientNotFoundException(String recipientUsername) {
        super("recipient with username " + recipientUsername + " does not exist");
    }
}
