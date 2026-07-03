package com.citassalud.integration;

import com.citassalud.application.port.WhatsAppGatewayPort.ResultadoEnvioWhatsApp;
import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.medico.Medico;
import com.citassalud.domain.paciente.Paciente;
import com.citassalud.infrastructure.whatsapp.WhatsAppCloudApiAdapter;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class WhatsAppCloudApiAdapterIT {

    private WireMockServer wireMockServer;
    private WhatsAppCloudApiAdapter adapter;

    private final Medico medico = new Medico(UUID.randomUUID(), "Dra. Ana Torres");
    private final Paciente paciente = new Paciente(UUID.randomUUID(), "Juan Pérez", "573001234567");
    private final Cita cita = Cita.agendar(UUID.randomUUID(), paciente, medico, Instant.now().plus(24, ChronoUnit.HOURS));

    @BeforeEach
    void configurar() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        adapter = new WhatsAppCloudApiAdapter(
                RestClient.builder(),
                wireMockServer.baseUrl(),
                "1234567890",
                "token-de-prueba",
                10L);
    }

    @AfterEach
    void limpiar() {
        wireMockServer.stop();
    }

    @Test
    void cuandoSeEnviaElRecordatorio_entoncesElCuerpoContieneNombreDelPacienteFechaHoraYNombreDelMedico() {
        Instant fechaCita = Instant.now().plus(24, ChronoUnit.HOURS);
        Medico medicoLocal = new Medico(UUID.randomUUID(), "Dra. Ana Torres");
        Paciente pacienteLocal = new Paciente(UUID.randomUUID(), "Juan Pérez", "573001234567");
        Cita citaLocal = Cita.agendar(UUID.randomUUID(), pacienteLocal, medicoLocal, fechaCita);

        wireMockServer.stubFor(post(urlEqualTo("/1234567890/messages"))
                .willReturn(okJson("{\"messages\":[{\"id\":\"wamid.FR002\"}]}")));

        adapter.enviarRecordatorio(pacienteLocal, medicoLocal, citaLocal);

        DateTimeFormatter formatter = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(Locale.of("es", "CO"));
        String fechaHoraFormateada = formatter.format(fechaCita.atZone(ZoneId.systemDefault()));

        wireMockServer.verify(postRequestedFor(urlEqualTo("/1234567890/messages"))
                .withRequestBody(matchingJsonPath("$.text.body", containing("Juan Pérez")))
                .withRequestBody(matchingJsonPath("$.text.body", containing(fechaHoraFormateada)))
                .withRequestBody(matchingJsonPath("$.text.body", containing("Dra. Ana Torres"))));
    }

    @Test
    void dadoUnEnvioExitoso_cuandoSeEnviaElRecordatorio_entoncesDevuelveElMensajeProveedorId() {
        wireMockServer.stubFor(post(urlEqualTo("/1234567890/messages"))
                .willReturn(okJson("""
                        {"messages":[{"id":"wamid.EXITO123"}]}
                        """)));

        ResultadoEnvioWhatsApp resultado = adapter.enviarRecordatorio(paciente, medico, cita);

        assertThat(resultado.exitoso()).isTrue();
        assertThat(resultado.mensajeProveedorId()).isEqualTo("wamid.EXITO123");
        assertThat(resultado.intentosRealizados()).isEqualTo(1);
    }

    @Test
    void dadoUnFalloTemporalSeguidoDeExito_cuandoSeEnviaElRecordatorio_entoncesReintentaYTermineExitoso() {
        wireMockServer.stubFor(post(urlEqualTo("/1234567890/messages"))
                .inScenario("reintento")
                .whenScenarioStateIs(com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("segundo-intento"));

        wireMockServer.stubFor(post(urlEqualTo("/1234567890/messages"))
                .inScenario("reintento")
                .whenScenarioStateIs("segundo-intento")
                .willReturn(okJson("""
                        {"messages":[{"id":"wamid.REINTENTO456"}]}
                        """)));

        ResultadoEnvioWhatsApp resultado = adapter.enviarRecordatorio(paciente, medico, cita);

        assertThat(resultado.exitoso()).isTrue();
        assertThat(resultado.mensajeProveedorId()).isEqualTo("wamid.REINTENTO456");
        assertThat(resultado.intentosRealizados()).isEqualTo(2);
    }

    @Test
    void dadoUnNumeroDeDestinoInvalido_cuandoSeEnviaElRecordatorio_entoncesFallaSinReintentar() {
        wireMockServer.stubFor(post(urlEqualTo("/1234567890/messages"))
                .willReturn(aResponse().withStatus(400).withBody("""
                        {"error":{"message":"numero invalido"}}
                        """)));

        ResultadoEnvioWhatsApp resultado = adapter.enviarRecordatorio(paciente, medico, cita);

        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.intentosRealizados()).isEqualTo(1);
        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/1234567890/messages")));
    }

    @Test
    void dadoUnFalloTemporalPersistente_cuandoSeAgotanLosReintentos_entoncesFallaTrasElMaximoDeIntentos() {
        wireMockServer.stubFor(post(urlEqualTo("/1234567890/messages"))
                .willReturn(aResponse().withStatus(503)));

        ResultadoEnvioWhatsApp resultado = adapter.enviarRecordatorio(paciente, medico, cita);

        assertThat(resultado.exitoso()).isFalse();
        assertThat(resultado.intentosRealizados()).isEqualTo(3);
        wireMockServer.verify(3, postRequestedFor(urlEqualTo("/1234567890/messages")));
    }

    @Test
    void cuandoSeEnviaLaConfirmacionDeCancelacion_entoncesSeHaceUnaSolaPeticionExitosa() {
        wireMockServer.stubFor(post(urlEqualTo("/1234567890/messages"))
                .willReturn(okJson("""
                        {"messages":[{"id":"wamid.CONFIRMACION"}]}
                        """)));

        adapter.enviarConfirmacionCancelacion(paciente, cita);

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/1234567890/messages")));
    }

    @Test
    void cuandoSeEnviaLaAclaracion_entoncesSeHaceUnaSolaPeticionExitosa() {
        wireMockServer.stubFor(post(urlEqualTo("/1234567890/messages"))
                .willReturn(okJson("""
                        {"messages":[{"id":"wamid.ACLARACION"}]}
                        """)));

        adapter.enviarAclaracion(paciente);

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/1234567890/messages")));
    }

    @Test
    void cuandoSeEnviaElRechazoPorVentanaVencida_entoncesSeHaceUnaSolaPeticionExitosa() {
        wireMockServer.stubFor(post(urlEqualTo("/1234567890/messages"))
                .willReturn(okJson("""
                        {"messages":[{"id":"wamid.RECHAZO"}]}
                        """)));

        adapter.enviarRechazoPorVentanaVencida(paciente);

        wireMockServer.verify(1, postRequestedFor(urlEqualTo("/1234567890/messages")));
    }
}
