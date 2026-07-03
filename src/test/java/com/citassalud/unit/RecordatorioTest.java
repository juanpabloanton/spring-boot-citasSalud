package com.citassalud.unit;

import com.citassalud.domain.recordatorio.EstadoEnvioRecordatorio;
import com.citassalud.domain.recordatorio.Recordatorio;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecordatorioTest {

    @Test
    void dadoUnRecordatorioPendiente_cuandoSeRegistranIntentosHastaElMaximo_entoncesAlcanzaElMaximoDeIntentos() {
        Recordatorio recordatorio = Recordatorio.pendiente(UUID.randomUUID(), UUID.randomUUID());

        assertThat(recordatorio.getEstadoEnvio()).isEqualTo(EstadoEnvioRecordatorio.PENDIENTE);
        assertThat(recordatorio.getCanal()).isEqualTo(Recordatorio.CANAL_WHATSAPP);
        assertThat(recordatorio.alcanzoMaximoIntentos()).isFalse();

        recordatorio.registrarIntento();
        recordatorio.registrarIntento();
        assertThat(recordatorio.alcanzoMaximoIntentos()).isFalse();

        recordatorio.registrarIntento();
        assertThat(recordatorio.getIntentos()).isEqualTo(3);
        assertThat(recordatorio.alcanzoMaximoIntentos()).isTrue();
    }

    @Test
    void dadoUnRecordatorio_cuandoSeMarcaEnviado_entoncesGuardaElMomentoYElMensajeProveedorId() {
        Recordatorio recordatorio = Recordatorio.pendiente(UUID.randomUUID(), UUID.randomUUID());
        Instant momento = Instant.now();

        recordatorio.marcarEnviado(momento, "wamid.123");

        assertThat(recordatorio.getEstadoEnvio()).isEqualTo(EstadoEnvioRecordatorio.ENVIADO);
        assertThat(recordatorio.getEnviadoEn()).isEqualTo(momento);
        assertThat(recordatorio.getMensajeProveedorId()).isEqualTo("wamid.123");
    }

    @Test
    void dadoUnRecordatorio_cuandoSeMarcaFallido_entoncesQuedaEnEstadoFallido() {
        Recordatorio recordatorio = Recordatorio.pendiente(UUID.randomUUID(), UUID.randomUUID());

        recordatorio.marcarFallido();

        assertThat(recordatorio.getEstadoEnvio()).isEqualTo(EstadoEnvioRecordatorio.FALLIDO);
    }

    @Test
    void dadoUnRecordatorio_cuandoSeMarcaSinNumeroValido_entoncesQuedaEnEseEstado() {
        Recordatorio recordatorio = Recordatorio.pendiente(UUID.randomUUID(), UUID.randomUUID());

        recordatorio.marcarSinNumeroValido();

        assertThat(recordatorio.getEstadoEnvio()).isEqualTo(EstadoEnvioRecordatorio.SIN_NUMERO_VALIDO);
    }

    @Test
    void dadosDosRecordatoriosConElMismoId_cuandoSeComparan_entoncesSonIguales() {
        UUID id = UUID.randomUUID();
        UUID citaId = UUID.randomUUID();
        Recordatorio recordatorio1 = Recordatorio.pendiente(id, citaId);
        Recordatorio recordatorio2 = new Recordatorio(id, citaId, Recordatorio.CANAL_WHATSAPP,
                EstadoEnvioRecordatorio.ENVIADO, Instant.now(), 1, "wamid.otro");

        assertThat(recordatorio1).isEqualTo(recordatorio2);
        assertThat(recordatorio1).hasSameHashCodeAs(recordatorio2);
        assertThat(recordatorio1).isNotEqualTo("no es un Recordatorio");
        assertThat(recordatorio1.getCitaId()).isEqualTo(citaId);
    }
}
