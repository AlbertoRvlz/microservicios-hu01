package com.hu01.productos.exception;

import java.time.Instant;

public record ApiError(Instant timestamp, int status, String error, String mensaje, String ruta) {
}
