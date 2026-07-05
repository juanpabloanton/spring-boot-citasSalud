package com.citassalud.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface RecordatorioSpringDataRepository extends JpaRepository<RecordatorioJpaEntity, UUID> {
}
