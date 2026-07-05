package com.citassalud.functional;

import com.citassalud.application.usecase.EnviarRecordatorioUseCase;
import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.cita.EstadoCita;
import com.citassalud.domain.medico.Medico;
import com.citassalud.domain.paciente.Paciente;
import com.citassalud.domain.recordatorio.EstadoEnvioRecordatorio;
import com.citassalud.infrastructure.scheduling.RecordatorioSchedulerJob;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RecordatorioStepDefinitions {

    private final InMemoryCitaRepositoryPort citaRepositoryPort = new InMemoryCitaRepositoryPort();
    private final FakeWhatsAppGatewayPort whatsAppGatewayPort = new FakeWhatsAppGatewayPort();
    private final InMemoryRecordatorioRepositoryPort recordatorioRepositoryPort = new InMemoryRecordatorioRepositoryPort();
    private final EnviarRecordatorioUseCase enviarRecordatorioUseCase =
            new EnviarRecordatorioUseCase(citaRepositoryPort, whatsAppGatewayPort, recordatorioRepositoryPort);
    private final RecordatorioSchedulerJob schedulerJob =
            new RecordatorioSchedulerJob(citaRepositoryPort, enviarRecordatorioUseCase);

    private final Map<String, Paciente> pacientesPorNombre = new HashMap<>();
    private final Medico medicoPorDefecto = new Medico(UUID.randomUUID(), "Dra. Ana Torres");

    @Dado("que el paciente {string} con WhatsApp {string} tiene una cita agendada con la médica {string} para dentro de 24 horas")
    public void pacienteConCitaAgendada(String nombrePaciente, String numeroWhatsapp, String nombreMedica) {
        Paciente paciente = new Paciente(UUID.randomUUID(), nombrePaciente, numeroWhatsapp);
        pacientesPorNombre.put(nombrePaciente, paciente);
        Medico medico = new Medico(UUID.randomUUID(), nombreMedica);
        citaRepositoryPort.guardar(
                Cita.agendar(UUID.randomUUID(), paciente, medico, Instant.now().plus(24, ChronoUnit.HOURS)));
    }

    @Dado("que el paciente {string} sin número de WhatsApp válido tiene una cita agendada para dentro de 24 horas")
    public void pacienteSinNumeroValidoConCitaAgendada(String nombrePaciente) {
        Paciente paciente = new Paciente(UUID.randomUUID(), nombrePaciente, null);
        pacientesPorNombre.put(nombrePaciente, paciente);
        citaRepositoryPort.guardar(
                Cita.agendar(UUID.randomUUID(), paciente, medicoPorDefecto, Instant.now().plus(24, ChronoUnit.HOURS)));
    }

    @Dado("que el paciente {string} con WhatsApp {string} tiene {int} citas agendadas para dentro de 24 horas")
    public void pacienteConVariasCitasAgendadas(String nombrePaciente, String numeroWhatsapp, int cantidad) {
        Paciente paciente = new Paciente(UUID.randomUUID(), nombrePaciente, numeroWhatsapp);
        pacientesPorNombre.put(nombrePaciente, paciente);
        for (int i = 0; i < cantidad; i++) {
            citaRepositoryPort.guardar(
                    Cita.agendar(UUID.randomUUID(), paciente, medicoPorDefecto, Instant.now().plus(24, ChronoUnit.HOURS)));
        }
    }

    @Cuando("el sistema ejecuta el envío de recordatorios pendientes")
    public void elSistemaEjecutaElEnvioDeRecordatorios() {
        schedulerJob.procesarRecordatoriosPendientes();
    }

    @Entonces("se envía un mensaje de WhatsApp con la fecha, la hora y el nombre de la médica al paciente {string}")
    public void seEnviaUnMensajeAlPaciente(String nombrePaciente) {
        assertThat(whatsAppGatewayPort.mensajesEnviadosA(pacientesPorNombre.get(nombrePaciente).getId())).isNotEmpty();
    }

    @Entonces("la cita del paciente {string} queda en estado {string}")
    public void laCitaDelPacienteQuedaEnEstado(String nombrePaciente, String estadoEsperado) {
        List<Cita> citas = citaRepositoryPort.buscarPorPacienteId(pacientesPorNombre.get(nombrePaciente).getId());
        assertThat(citas).isNotEmpty();
        assertThat(citas.get(0).getEstado()).isEqualTo(EstadoCita.valueOf(estadoEsperado));
    }

    @Entonces("el sistema registra el intento fallido por número inválido para el paciente {string}")
    public void seRegistraElIntentoFallido(String nombrePaciente) {
        UUID pacienteId = pacientesPorNombre.get(nombrePaciente).getId();
        assertThat(whatsAppGatewayPort.mensajesEnviadosA(pacienteId)).isEmpty();

        UUID citaId = citaRepositoryPort.buscarPorPacienteId(pacienteId).get(0).getId();
        assertThat(recordatorioRepositoryPort.buscarPorCitaId(citaId))
                .anyMatch(r -> r.getEstadoEnvio() == EstadoEnvioRecordatorio.SIN_NUMERO_VALIDO);
    }

    @Entonces("la cita del paciente {string} permanece en estado {string}")
    public void laCitaPermaneceEnEstado(String nombrePaciente, String estadoEsperado) {
        laCitaDelPacienteQuedaEnEstado(nombrePaciente, estadoEsperado);
    }

    @Entonces("se envían {int} recordatorios independientes al paciente {string}")
    public void seEnvianRecordatoriosIndependientes(int cantidad, String nombrePaciente) {
        UUID pacienteId = pacientesPorNombre.get(nombrePaciente).getId();
        assertThat(whatsAppGatewayPort.mensajesEnviadosA(pacienteId)).hasSize(cantidad);
        assertThat(citaRepositoryPort.buscarPorPacienteId(pacienteId))
                .allMatch(c -> c.getEstado() == EstadoCita.RECORDATORIO_ENVIADO);
    }
}
