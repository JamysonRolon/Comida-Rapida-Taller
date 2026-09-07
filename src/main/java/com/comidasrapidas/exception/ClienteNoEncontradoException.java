package com.comidasrapidas.exception;

public class ClienteNoEncontradoException extends RuntimeException {
    public ClienteNoEncontradoException() {
        super("El cliente no existe.");
    }
}

