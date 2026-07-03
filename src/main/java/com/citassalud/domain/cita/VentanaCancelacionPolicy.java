package com.citassalud.domain.cita;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * FR-008: la cancelación por WhatsApp solo se acepta hasta 2 horas antes del turno.
 * Regla única y reutilizable por ambos casos de uso que dependen de la ventana de tiempo.
 */
public class VentanaCancelacionPolicy {

    private static final Duration VENTANA_MINIMA = Duration.ofHours(2);

    public boolean permiteCancelacion(Cita cita, Instant ahora) {
        Objects.requireNonNull(cita, "cita no puede ser nula");
        Objects.requireNonNull(ahora, "ahora no puede ser nulo");
        Duration margen = Duration.between(ahora, cita.getFechaHora());
        return margen.compareTo(VENTANA_MINIMA) >= 0;
    }
}
