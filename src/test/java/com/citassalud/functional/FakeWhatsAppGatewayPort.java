package com.citassalud.functional;

import com.citassalud.application.port.WhatsAppGatewayPort;
import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.medico.Medico;
import com.citassalud.domain.paciente.Paciente;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class FakeWhatsAppGatewayPort implements WhatsAppGatewayPort {

    private final Map<UUID, List<String>> mensajesPorPaciente = new HashMap<>();
    private int contadorMensajes = 0;

    @Override
    public ResultadoEnvioWhatsApp enviarRecordatorio(Paciente paciente, Medico medico, Cita cita) {
        String mensajeId = "wamid.test." + (++contadorMensajes);
        registrarMensaje(paciente.getId(), "recordatorio:" + mensajeId);
        return new ResultadoEnvioWhatsApp(true, mensajeId, 1);
    }

    @Override
    public void enviarConfirmacionCancelacion(Paciente paciente, Cita cita) {
        registrarMensaje(paciente.getId(), "confirmacion-cancelacion");
    }

    @Override
    public void enviarAclaracion(Paciente paciente) {
        registrarMensaje(paciente.getId(), "aclaracion");
    }

    @Override
    public void enviarRechazoPorVentanaVencida(Paciente paciente) {
        registrarMensaje(paciente.getId(), "rechazo-ventana-vencida");
    }

    private void registrarMensaje(UUID pacienteId, String mensaje) {
        mensajesPorPaciente.computeIfAbsent(pacienteId, k -> new ArrayList<>()).add(mensaje);
    }

    List<String> mensajesEnviadosA(UUID pacienteId) {
        return mensajesPorPaciente.getOrDefault(pacienteId, List.of());
    }
}
