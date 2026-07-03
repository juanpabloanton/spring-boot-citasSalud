package com.citassalud.functional;

import com.citassalud.application.usecase.CancelarCitaUseCase;
import com.citassalud.application.usecase.ResultadoCancelacion;
import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.cita.EstadoCita;
import com.citassalud.domain.cita.VentanaCancelacionPolicy;
import com.citassalud.domain.medico.Medico;
import com.citassalud.domain.paciente.Paciente;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CancelacionStepDefinitions {

    private static final String MENSAJE_ORIGEN = "wamid.recordatorio.hilo";

    private final InMemoryCitaRepositoryPort citaRepositoryPort = new InMemoryCitaRepositoryPort();
    private final FakeWhatsAppGatewayPort whatsAppGatewayPort = new FakeWhatsAppGatewayPort();
    private final CancelarCitaUseCase cancelarCitaUseCase =
            new CancelarCitaUseCase(citaRepositoryPort, whatsAppGatewayPort, new VentanaCancelacionPolicy());

    private final Map<String, Cita> citasPorPaciente = new HashMap<>();
    private final Map<String, Paciente> pacientesPorNombre = new HashMap<>();
    private ResultadoCancelacion ultimoResultado;

    @Dado("que el paciente {string} recibió el recordatorio de WhatsApp de su cita")
    public void pacienteRecibioElRecordatorio(String nombrePaciente) {
        Medico medico = new Medico(UUID.randomUUID(), "Dra. Ana Torres");
        Paciente paciente = new Paciente(UUID.randomUUID(), nombrePaciente, "573001234567");
        pacientesPorNombre.put(nombrePaciente, paciente);

        Cita cita = Cita.agendar(UUID.randomUUID(), paciente, medico, Instant.now().plus(5, ChronoUnit.HOURS));
        cita.registrarEnvioRecordatorio(Instant.now(), MENSAJE_ORIGEN + ":" + paciente.getId());
        citaRepositoryPort.guardar(cita);
        citasPorPaciente.put(nombrePaciente, cita);
    }

    @Cuando("responde {string} al recordatorio")
    public void respondeAlRecordatorio(String textoRespuesta) {
        Cita cita = citasPorPaciente.values().iterator().next();
        ultimoResultado = cancelarCitaUseCase.procesarRespuesta(cita.getUltimoMensajeWhatsappId(), textoRespuesta);
    }

    @Entonces("la cita queda anulada y la franja vuelve a estar disponible en la agenda")
    public void laCitaQuedaAnulada() {
        assertThat(ultimoResultado).isEqualTo(ResultadoCancelacion.CANCELADA);
        Cita cita = citasPorPaciente.values().iterator().next();
        assertThat(cita.getEstado()).isEqualTo(EstadoCita.CANCELADA);
    }

    @Entonces("la cita permanece sin cambios")
    public void laCitaPermaneceSinCambios() {
        Cita cita = citasPorPaciente.values().iterator().next();
        assertThat(cita.getEstado()).isEqualTo(EstadoCita.RECORDATORIO_ENVIADO);
    }

    @Entonces("se le reenvía al paciente {string} una aclaración de las opciones válidas")
    public void seLeReenviaLaAclaracion(String nombrePaciente) {
        assertThat(ultimoResultado).isEqualTo(ResultadoCancelacion.NO_RECONOCIDA);
        Paciente paciente = pacientesPorNombre.get(nombrePaciente);
        assertThat(whatsAppGatewayPort.mensajesEnviadosA(paciente.getId())).contains("aclaracion");
    }

    @Entonces("el sistema confirma por WhatsApp al paciente {string} que la cita fue cancelada exitosamente")
    public void seConfirmaLaCancelacionPorWhatsapp(String nombrePaciente) {
        assertThat(ultimoResultado).isEqualTo(ResultadoCancelacion.CANCELADA);
        Paciente paciente = pacientesPorNombre.get(nombrePaciente);
        assertThat(whatsAppGatewayPort.mensajesEnviadosA(paciente.getId())).contains("confirmacion-cancelacion");
    }
}
