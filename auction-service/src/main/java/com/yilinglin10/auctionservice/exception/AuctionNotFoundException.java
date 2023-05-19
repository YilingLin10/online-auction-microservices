package com.yilinglin10.auctionservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class AuctionNotFoundException extends RuntimeException {
    public AuctionNotFoundException(Long id) {
        super("cannot find auction with id "+ id);
    }
}
