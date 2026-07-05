package com.citassalud.functional;

import com.citassalud.application.port.RecordatorioRepositoryPort;
import com.citassalud.domain.recordatorio.Recordatorio;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryRecordatorioRepositoryPort implements RecordatorioRepositoryPort {

    private final List<Recordatorio> recordatorios = new ArrayList<>();

    @Override
    public Recordatorio guardar(Recordatorio recordatorio) {
        recordatorios.add(recordatorio);
        return recordatorio;
    }

    public List<Recordatorio> buscarPorCitaId(UUID citaId) {
        return recordatorios.stream().filter(r -> r.getCitaId().equals(citaId)).toList();
    }
}
