package com.citassalud.application.port;

import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.medico.Medico;
import com.citassalud.domain.paciente.Paciente;

public interface WhatsAppGatewayPort {

    /**
     * Envía el recordatorio de cita (FR-002: incluye fecha, hora y nombre de la médica/médico).
     */
    ResultadoEnvioWhatsApp enviarRecordatorio(Paciente paciente, Medico medico, Cita cita);

    /**
     * FR-005: confirma al paciente que la cancelación fue procesada.
     */
    void enviarConfirmacionCancelacion(Paciente paciente, Cita cita);

    /**
     * FR-003a: reenvía las opciones válidas cuando la respuesta del paciente no fue reconocida.
     */
    void enviarAclaracion(Paciente paciente);

    /**
     * FR-008: informa que la cancelación por WhatsApp ya no se acepta (ventana de 2h vencida)
     * y que debe contactar directamente al centro de salud.
     */
    void enviarRechazoPorVentanaVencida(Paciente paciente);

    record ResultadoEnvioWhatsApp(boolean exitoso, String mensajeProveedorId, int intentosRealizados) {
    }
}
