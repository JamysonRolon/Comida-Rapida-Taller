package com.comidasrapidas.exception;

public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException() {
        super("No existe suficiente stock para realizar la venta.");
    }
}

