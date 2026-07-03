package com.citassalud.functional;

import com.citassalud.application.port.CitaRepositoryPort;
import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.cita.EstadoCita;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

class InMemoryCitaRepositoryPort implements CitaRepositoryPort {

    private final Map<UUID, Cita> citas = new LinkedHashMap<>();

    @Override
    public Optional<Cita> buscarPorId(UUID id) {
        return Optional.ofNullable(citas.get(id));
    }

    @Override
    public Optional<Cita> buscarPorMensajeWhatsappId(String mensajeProveedorId) {
        return citas.values().stream()
                .filter(c -> mensajeProveedorId.equals(c.getUltimoMensajeWhatsappId()))
                .findFirst();
    }

    @Override
    public List<Cita> buscarAgendadasEnVentana(Instant desde, Instant hasta) {
        return citas.values().stream()
                .filter(c -> c.getEstado() == EstadoCita.AGENDADA)
                .filter(c -> !c.getFechaHora().isBefore(desde) && !c.getFechaHora().isAfter(hasta))
                .collect(Collectors.toList());
    }

    @Override
    public Cita guardar(Cita cita) {
        citas.put(cita.getId(), cita);
        return cita;
    }

    List<Cita> buscarPorPacienteId(UUID pacienteId) {
        return citas.values().stream()
                .filter(c -> c.getPaciente().getId().equals(pacienteId))
                .collect(Collectors.toList());
    }
}
