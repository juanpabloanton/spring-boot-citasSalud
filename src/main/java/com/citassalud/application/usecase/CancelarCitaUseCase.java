package com.citassalud.application.usecase;

import com.citassalud.application.port.CitaRepositoryPort;
import com.citassalud.application.port.WhatsAppGatewayPort;
import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.cita.VentanaCancelacionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * FR-003/FR-003a/FR-004/FR-005/FR-008/FR-009: procesa la respuesta entrante de WhatsApp
 * a un recordatorio, resolviendo la Cita por el hilo específico y aplicando la ventana
 * mínima de cancelación.
 */
public class CancelarCitaUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(CancelarCitaUseCase.class);
    private static final String PALABRA_CLAVE_CANCELACION = "CANCELAR";

    private final CitaRepositoryPort citaRepositoryPort;
    private final WhatsAppGatewayPort whatsAppGatewayPort;
    private final VentanaCancelacionPolicy ventanaCancelacionPolicy;
    private final Supplier<Instant> reloj;

    public CancelarCitaUseCase(CitaRepositoryPort citaRepositoryPort, WhatsAppGatewayPort whatsAppGatewayPort,
                                VentanaCancelacionPolicy ventanaCancelacionPolicy) {
        this(citaRepositoryPort, whatsAppGatewayPort, ventanaCancelacionPolicy, Instant::now);
    }

    CancelarCitaUseCase(CitaRepositoryPort citaRepositoryPort, WhatsAppGatewayPort whatsAppGatewayPort,
                         VentanaCancelacionPolicy ventanaCancelacionPolicy, Supplier<Instant> reloj) {
        this.citaRepositoryPort = citaRepositoryPort;
        this.whatsAppGatewayPort = whatsAppGatewayPort;
        this.ventanaCancelacionPolicy = ventanaCancelacionPolicy;
        this.reloj = reloj;
    }

    /**
     * FR-009: la cita se resuelve exclusivamente por el hilo del recordatorio
     * ({@code mensajeProveedorIdOrigen}), nunca por búsqueda ambigua entre citas del paciente.
     */
    public ResultadoCancelacion procesarRespuesta(String mensajeProveedorIdOrigen, String textoRespuesta) {
        Optional<Cita> citaOpt = citaRepositoryPort.buscarPorMensajeWhatsappId(mensajeProveedorIdOrigen);
        if (citaOpt.isEmpty()) {
            LOG.warn("No se encontró una cita asociada al hilo de WhatsApp {}", mensajeProveedorIdOrigen);
            return ResultadoCancelacion.HILO_NO_ENCONTRADO;
        }
        Cita cita = citaOpt.get();

        if (!esPalabraClaveCancelacion(textoRespuesta)) {
            whatsAppGatewayPort.enviarAclaracion(cita.getPaciente());
            return ResultadoCancelacion.NO_RECONOCIDA;
        }

        if (!ventanaCancelacionPolicy.permiteCancelacion(cita, reloj.get())) {
            whatsAppGatewayPort.enviarRechazoPorVentanaVencida(cita.getPaciente());
            return ResultadoCancelacion.FUERA_DE_VENTANA;
        }

        cita.cancelar();
        citaRepositoryPort.guardar(cita);
        whatsAppGatewayPort.enviarConfirmacionCancelacion(cita.getPaciente(), cita);
        return ResultadoCancelacion.CANCELADA;
    }

    private boolean esPalabraClaveCancelacion(String texto) {
        return texto != null && texto.trim().equalsIgnoreCase(PALABRA_CLAVE_CANCELACION);
    }
}
