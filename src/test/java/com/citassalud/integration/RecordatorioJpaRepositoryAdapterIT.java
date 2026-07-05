package com.citassalud.integration;

import com.citassalud.domain.recordatorio.EstadoEnvioRecordatorio;
import com.citassalud.domain.recordatorio.Recordatorio;
import com.citassalud.infrastructure.persistence.RecordatorioJpaRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(RecordatorioJpaRepositoryAdapter.class)
class RecordatorioJpaRepositoryAdapterIT {

    @Autowired
    private RecordatorioJpaRepositoryAdapter adapter;

    @Test
    void dadoUnRecordatorioEnviado_cuandoSeGuarda_entoncesQuedaPersistidoParaAuditoria() {
        UUID citaId = UUID.randomUUID();
        Recordatorio recordatorio = Recordatorio.pendiente(UUID.randomUUID(), citaId);
        recordatorio.registrarIntento();
        recordatorio.marcarEnviado(Instant.now(), "wamid.auditoria123");

        Recordatorio guardado = adapter.guardar(recordatorio);

        assertThat(guardado.getId()).isEqualTo(recordatorio.getId());
        assertThat(guardado.getCitaId()).isEqualTo(citaId);
        assertThat(guardado.getEstadoEnvio()).isEqualTo(EstadoEnvioRecordatorio.ENVIADO);
        assertThat(guardado.getMensajeProveedorId()).isEqualTo("wamid.auditoria123");
    }

    @Test
    void dadoUnRecordatorioSinNumeroValido_cuandoSeGuarda_entoncesQuedaPersistidoElEstadoDeFallo() {
        UUID citaId = UUID.randomUUID();
        Recordatorio recordatorio = Recordatorio.pendiente(UUID.randomUUID(), citaId);
        recordatorio.marcarSinNumeroValido();

        Recordatorio guardado = adapter.guardar(recordatorio);

        assertThat(guardado.getEstadoEnvio()).isEqualTo(EstadoEnvioRecordatorio.SIN_NUMERO_VALIDO);
    }
}
