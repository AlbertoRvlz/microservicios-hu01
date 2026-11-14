package com.hu01.productos.exception;

public class DatoDuplicadoException extends RuntimeException {
    public DatoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
