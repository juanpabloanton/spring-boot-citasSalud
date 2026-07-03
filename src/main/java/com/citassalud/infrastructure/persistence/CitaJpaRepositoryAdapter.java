package com.citassalud.infrastructure.persistence;

import com.citassalud.application.port.CitaRepositoryPort;
import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.cita.EstadoCita;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CitaJpaRepositoryAdapter implements CitaRepositoryPort {

    private final CitaSpringDataRepository springDataRepository;

    public CitaJpaRepositoryAdapter(CitaSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<Cita> buscarPorId(UUID id) {
        return springDataRepository.findById(id).map(CitaJpaEntity::aDominio);
    }

    @Override
    public Optional<Cita> buscarPorMensajeWhatsappId(String mensajeProveedorId) {
        return springDataRepository.findByUltimoMensajeWhatsappId(mensajeProveedorId).map(CitaJpaEntity::aDominio);
    }

    @Override
    public List<Cita> buscarAgendadasEnVentana(Instant desde, Instant hasta) {
        return springDataRepository.findByEstadoAndFechaHoraBetween(EstadoCita.AGENDADA, desde, hasta).stream()
                .map(CitaJpaEntity::aDominio)
                .toList();
    }

    @Override
    public Cita guardar(Cita cita) {
        CitaJpaEntity guardada = springDataRepository.save(CitaJpaEntity.desdeDominio(cita));
        return guardada.aDominio();
    }
}
