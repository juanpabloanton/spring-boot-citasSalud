package com.citassalud.application.port;

import com.citassalud.domain.cita.Cita;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CitaRepositoryPort {

    Optional<Cita> buscarPorId(UUID id);

    /**
     * FR-009: resuelve la Cita asociada al hilo de WhatsApp a partir del identificador
     * de mensaje de proveedor registrado en el último recordatorio enviado.
     */
    Optional<Cita> buscarPorMensajeWhatsappId(String mensajeProveedorId);

    List<Cita> buscarAgendadasEnVentana(Instant desde, Instant hasta);

    Cita guardar(Cita cita);
}
