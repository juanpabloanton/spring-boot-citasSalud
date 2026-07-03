package com.citassalud.application.usecase;

import com.citassalud.application.port.CitaRepositoryPort;
import com.citassalud.application.port.WhatsAppGatewayPort;
import com.citassalud.application.port.WhatsAppGatewayPort.ResultadoEnvioWhatsApp;
import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.recordatorio.Recordatorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * FR-001/FR-002/FR-006/FR-007: envía el recordatorio de una cita agendada y deja
 * constancia auditable de cada intento (enviado, fallido, sin número válido).
 */
public class EnviarRecordatorioUseCase {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("com.citassalud.auditoria.recordatorio");

    private final CitaRepositoryPort citaRepositoryPort;
    private final WhatsAppGatewayPort whatsAppGatewayPort;
    private final Supplier<Instant> reloj;

    public EnviarRecordatorioUseCase(CitaRepositoryPort citaRepositoryPort, WhatsAppGatewayPort whatsAppGatewayPort) {
        this(citaRepositoryPort, whatsAppGatewayPort, Instant::now);
    }

    EnviarRecordatorioUseCase(CitaRepositoryPort citaRepositoryPort, WhatsAppGatewayPort whatsAppGatewayPort,
                               Supplier<Instant> reloj) {
        this.citaRepositoryPort = citaRepositoryPort;
        this.whatsAppGatewayPort = whatsAppGatewayPort;
        this.reloj = reloj;
    }

    public void ejecutar(UUID citaId) {
        Cita cita = citaRepositoryPort.buscarPorId(citaId)
                .orElseThrow(() -> new NoSuchElementException("Cita no encontrada: " + citaId));

        Recordatorio recordatorio = Recordatorio.pendiente(UUID.randomUUID(), citaId);

        if (!cita.getPaciente().tieneNumeroWhatsappValido()) {
            recordatorio.marcarSinNumeroValido();
            registrarAuditoria(recordatorio, cita,
                    "SIN_NUMERO_VALIDO - se notifica al área administrativa");
            return;
        }

        recordatorio.registrarIntento();
        ResultadoEnvioWhatsApp resultado =
                whatsAppGatewayPort.enviarRecordatorio(cita.getPaciente(), cita.getMedico(), cita);

        if (!resultado.exitoso()) {
            recordatorio.marcarFallido();
            registrarAuditoria(recordatorio, cita, "FALLIDO - se notifica al área administrativa");
            return;
        }

        Instant momento = reloj.get();
        recordatorio.marcarEnviado(momento, resultado.mensajeProveedorId());
        cita.registrarEnvioRecordatorio(momento, resultado.mensajeProveedorId());
        citaRepositoryPort.guardar(cita);

        registrarAuditoria(recordatorio, cita, "ENVIADO");
    }

    private void registrarAuditoria(Recordatorio recordatorio, Cita cita, String detalle) {
        AUDITORIA.info("Recordatorio {} citaId={} pacienteId={} intentos={} mensajeProveedorId={} - {}",
                recordatorio.getEstadoEnvio(), cita.getId(), cita.getPaciente().getId(),
                recordatorio.getIntentos(), recordatorio.getMensajeProveedorId(), detalle);
    }
}
