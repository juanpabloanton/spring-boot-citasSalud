package com.citassalud.infrastructure.persistence;

import com.citassalud.domain.cita.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface CitaSpringDataRepository extends JpaRepository<CitaJpaEntity, UUID> {

    List<CitaJpaEntity> findByEstadoAndFechaHoraBetween(EstadoCita estado, Instant desde, Instant hasta);

    Optional<CitaJpaEntity> findByUltimoMensajeWhatsappId(String ultimoMensajeWhatsappId);
}
