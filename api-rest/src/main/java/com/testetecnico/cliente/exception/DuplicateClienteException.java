package com.testetecnico.cliente.exception;

public class DuplicateClienteException extends RuntimeException {
    public DuplicateClienteException(String message) {
        super(message);
    }
}
