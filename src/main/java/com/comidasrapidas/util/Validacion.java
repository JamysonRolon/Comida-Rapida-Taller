package com.comidasrapidas.util;

import com.comidasrapidas.exception.DatosInvalidosException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class Validacion {
    private Validacion() {
    }

    public static String texto(String valor, String campo, int maximo) {
        String limpio = opcional(valor, campo, maximo);
        exigir(!limpio.isEmpty(), campo + " es obligatorio.");
        return limpio;
    }

    public static String opcional(String valor, String campo, int maximo) {
        String limpio = valor == null ? "" : valor.trim();
        exigir(limpio.length() <= maximo, campo + ": máximo " + maximo + " caracteres.");
        exigir(limpio.chars().noneMatch(Character::isISOControl), campo + " contiene caracteres no permitidos.");
        return limpio;
    }

    public static String correo(String valor) {
        String limpio = opcional(valor, "Correo", 150);
        exigir(limpio.isEmpty() || limpio.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"),
                "El formato del correo no es válido.");
        return limpio;
    }

    public static BigDecimal precio(BigDecimal valor) {
        exigir(valor != null && valor.signum() > 0, "El precio debe ser mayor que cero.");
        try {
            BigDecimal dinero = valor.setScale(2, RoundingMode.UNNECESSARY);
            exigir(dinero.precision() <= 14, "El importe supera el máximo permitido.");
            return dinero;
        } catch (ArithmeticException e) {
            throw new DatosInvalidosException("El precio admite hasta dos decimales.");
        }
    }

    public static String username(String valor) {
        String limpio = texto(valor, "Usuario", 60).toLowerCase(Locale.ROOT);
        exigir(limpio.matches("[a-z0-9._-]+"), "Usuario: use letras sin tildes, números, punto, guion o guion bajo.");
        return limpio;
    }

    public static void password(String valor) {
        exigir(valor != null && valor.length() >= 10, "La contraseña debe tener al menos 10 caracteres.");
        exigir(valor.getBytes(StandardCharsets.UTF_8).length <= 72,
                "La contraseña no puede superar 72 bytes UTF-8.");
    }

    public static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new DatosInvalidosException(mensaje);
        }
    }
}
