package com.citassalud.domain.recordatorio;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa un intento de envío de recordatorio de WhatsApp asociado a una {@code Cita}.
 * Se usa dentro de {@code EnviarRecordatorioUseCase} para acumular el estado de un envío
 * (reintentos, resultado) y alimentar el registro de auditoría (FR-006).
 */
public class Recordatorio {

    public static final String CANAL_WHATSAPP = "WHATSAPP";
    private static final int MAX_INTENTOS = 3;

    private final UUID id;
    private final UUID citaId;
    private final String canal;
    private EstadoEnvioRecordatorio estadoEnvio;
    private Instant enviadoEn;
    private int intentos;
    private String mensajeProveedorId;

    public Recordatorio(UUID id, UUID citaId, String canal, EstadoEnvioRecordatorio estadoEnvio,
                         Instant enviadoEn, int intentos, String mensajeProveedorId) {
        this.id = Objects.requireNonNull(id, "id no puede ser nulo");
        this.citaId = Objects.requireNonNull(citaId, "citaId no puede ser nulo");
        this.canal = Objects.requireNonNull(canal, "canal no puede ser nulo");
        this.estadoEnvio = Objects.requireNonNull(estadoEnvio, "estadoEnvio no puede ser nulo");
        this.enviadoEn = enviadoEn;
        this.intentos = intentos;
        this.mensajeProveedorId = mensajeProveedorId;
    }

    public static Recordatorio pendiente(UUID id, UUID citaId) {
        return new Recordatorio(id, citaId, CANAL_WHATSAPP, EstadoEnvioRecordatorio.PENDIENTE, null, 0, null);
    }

    public void registrarIntento() {
        this.intentos++;
    }

    public boolean alcanzoMaximoIntentos() {
        return intentos >= MAX_INTENTOS;
    }

    public void marcarEnviado(Instant momento, String mensajeProveedorId) {
        this.estadoEnvio = EstadoEnvioRecordatorio.ENVIADO;
        this.enviadoEn = Objects.requireNonNull(momento, "momento no puede ser nulo");
        this.mensajeProveedorId = mensajeProveedorId;
    }

    public void marcarFallido() {
        this.estadoEnvio = EstadoEnvioRecordatorio.FALLIDO;
    }

    public void marcarSinNumeroValido() {
        this.estadoEnvio = EstadoEnvioRecordatorio.SIN_NUMERO_VALIDO;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCitaId() {
        return citaId;
    }

    public String getCanal() {
        return canal;
    }

    public EstadoEnvioRecordatorio getEstadoEnvio() {
        return estadoEnvio;
    }

    public Instant getEnviadoEn() {
        return enviadoEn;
    }

    public int getIntentos() {
        return intentos;
    }

    public String getMensajeProveedorId() {
        return mensajeProveedorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recordatorio that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
