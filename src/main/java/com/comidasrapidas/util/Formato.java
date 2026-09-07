package com.comidasrapidas.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formato {
    private Formato() {
    }

    public static String dinero(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO")).format(valor);
    }

    public static String fecha(Instant fecha) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.of("America/Bogota")).format(fecha);
    }

    public static String estado(boolean activo) {
        return activo ? "Activo" : "Inactivo";
    }
}
