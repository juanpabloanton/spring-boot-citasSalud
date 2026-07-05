package com.citassalud.infrastructure.persistence;

import com.citassalud.application.port.RecordatorioRepositoryPort;
import com.citassalud.domain.recordatorio.Recordatorio;
import org.springframework.stereotype.Component;

@Component
public class RecordatorioJpaRepositoryAdapter implements RecordatorioRepositoryPort {

    private final RecordatorioSpringDataRepository springDataRepository;

    public RecordatorioJpaRepositoryAdapter(RecordatorioSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Recordatorio guardar(Recordatorio recordatorio) {
        RecordatorioJpaEntity guardado = springDataRepository.save(RecordatorioJpaEntity.desdeDominio(recordatorio));
        return guardado.aDominio();
    }
}
