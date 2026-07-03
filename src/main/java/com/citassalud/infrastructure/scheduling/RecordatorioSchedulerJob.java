package com.citassalud.infrastructure.scheduling;

import com.citassalud.application.port.CitaRepositoryPort;
import com.citassalud.application.usecase.EnviarRecordatorioUseCase;
import com.citassalud.domain.cita.Cita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * research.md §2: poller periódico que detecta citas dentro de la ventana de 24h
 * y dispara el envío del recordatorio. Idempotente: solo procesa citas AGENDADA.
 */
@Component
public class RecordatorioSchedulerJob {

    private static final Logger LOG = LoggerFactory.getLogger(RecordatorioSchedulerJob.class);
    private static final Duration VENTANA_OBJETIVO = Duration.ofHours(24);
    private static final Duration MARGEN = Duration.ofMinutes(5);

    private final CitaRepositoryPort citaRepositoryPort;
    private final EnviarRecordatorioUseCase enviarRecordatorioUseCase;

    public RecordatorioSchedulerJob(CitaRepositoryPort citaRepositoryPort,
                                     EnviarRecordatorioUseCase enviarRecordatorioUseCase) {
        this.citaRepositoryPort = citaRepositoryPort;
        this.enviarRecordatorioUseCase = enviarRecordatorioUseCase;
    }

    @Scheduled(fixedRateString = "${whatsapp.recordatorio.intervalo-sondeo-ms:300000}")
    public void procesarRecordatoriosPendientes() {
        Instant ahora = Instant.now();
        Instant desde = ahora.plus(VENTANA_OBJETIVO).minus(MARGEN);
        Instant hasta = ahora.plus(VENTANA_OBJETIVO).plus(MARGEN);

        for (Cita cita : citaRepositoryPort.buscarAgendadasEnVentana(desde, hasta)) {
            try {
                enviarRecordatorioUseCase.ejecutar(cita.getId());
            } catch (RuntimeException ex) {
                LOG.error("Error inesperado al procesar el recordatorio de la cita {}: {}", cita.getId(), ex.getMessage(), ex);
            }
        }
    }
}
