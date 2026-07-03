package com.citassalud.unit;

import com.citassalud.application.port.CitaRepositoryPort;
import com.citassalud.application.port.WhatsAppGatewayPort;
import com.citassalud.application.usecase.CancelarCitaUseCase;
import com.citassalud.application.usecase.ResultadoCancelacion;
import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.cita.EstadoCita;
import com.citassalud.domain.cita.VentanaCancelacionPolicy;
import com.citassalud.domain.medico.Medico;
import com.citassalud.domain.paciente.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CancelarCitaUseCaseTest {

    private static final String MENSAJE_ORIGEN = "wamid.recordatorio.1";

    private CitaRepositoryPort citaRepositoryPort;
    private WhatsAppGatewayPort whatsAppGatewayPort;
    private CancelarCitaUseCase useCase;

    private Medico medico;
    private Paciente paciente;

    @BeforeEach
    void configurar() {
        citaRepositoryPort = mock(CitaRepositoryPort.class);
        whatsAppGatewayPort = mock(WhatsAppGatewayPort.class);
        useCase = new CancelarCitaUseCase(citaRepositoryPort, whatsAppGatewayPort, new VentanaCancelacionPolicy());
        medico = new Medico(UUID.randomUUID(), "Dra. Ana Torres");
        paciente = new Paciente(UUID.randomUUID(), "Juan Pérez", "573001234567");
    }

    private Cita citaConRecordatorioEnviado(Instant fechaHora) {
        Cita cita = Cita.agendar(UUID.randomUUID(), paciente, medico, fechaHora);
        cita.registrarEnvioRecordatorio(Instant.now(), MENSAJE_ORIGEN);
        return cita;
    }

    @Test
    void dadoElPacienteRecibioElRecordatorio_cuandoRespondeCancelar_entoncesLaCitaQuedaAnuladaYSeConfirmaPorWhatsapp() {
        Cita cita = citaConRecordatorioEnviado(Instant.now().plus(3, ChronoUnit.HOURS));
        when(citaRepositoryPort.buscarPorMensajeWhatsappId(MENSAJE_ORIGEN)).thenReturn(Optional.of(cita));

        ResultadoCancelacion resultado = useCase.procesarRespuesta(MENSAJE_ORIGEN, "CANCELAR");

        assertThat(resultado).isEqualTo(ResultadoCancelacion.CANCELADA);
        assertThat(cita.getEstado()).isEqualTo(EstadoCita.CANCELADA);
        verify(citaRepositoryPort).guardar(cita);
        verify(whatsAppGatewayPort).enviarConfirmacionCancelacion(paciente, cita);
    }

    @Test
    void dadaUnaRespuestaNoReconocida_cuandoSeProcesa_entoncesLaCitaNoCambiaYSeReenviaLaAclaracion() {
        Cita cita = citaConRecordatorioEnviado(Instant.now().plus(3, ChronoUnit.HOURS));
        when(citaRepositoryPort.buscarPorMensajeWhatsappId(MENSAJE_ORIGEN)).thenReturn(Optional.of(cita));

        ResultadoCancelacion resultado = useCase.procesarRespuesta(MENSAJE_ORIGEN, "no puedo ir pero no cancelo");

        assertThat(resultado).isEqualTo(ResultadoCancelacion.NO_RECONOCIDA);
        assertThat(cita.getEstado()).isEqualTo(EstadoCita.RECORDATORIO_ENVIADO);
        verify(citaRepositoryPort, never()).guardar(any());
        verify(whatsAppGatewayPort).enviarAclaracion(paciente);
    }

    @Test
    void dadaLaPalabraClaveConEspaciosYMinusculas_cuandoSeProcesa_entoncesSeReconoceLaCancelacion() {
        Cita cita = citaConRecordatorioEnviado(Instant.now().plus(3, ChronoUnit.HOURS));
        when(citaRepositoryPort.buscarPorMensajeWhatsappId(MENSAJE_ORIGEN)).thenReturn(Optional.of(cita));

        ResultadoCancelacion resultado = useCase.procesarRespuesta(MENSAJE_ORIGEN, "  cancelar  ");

        assertThat(resultado).isEqualTo(ResultadoCancelacion.CANCELADA);
    }

    @Test
    void dadaUnaCitaAMenosDe2HorasDelTurno_cuandoResponteCancelar_entoncesSeRechazaLaCancelacionYSeIndicaContactarAlCentro() {
        Cita cita = citaConRecordatorioEnviado(Instant.now().plus(1, ChronoUnit.HOURS));
        when(citaRepositoryPort.buscarPorMensajeWhatsappId(MENSAJE_ORIGEN)).thenReturn(Optional.of(cita));

        ResultadoCancelacion resultado = useCase.procesarRespuesta(MENSAJE_ORIGEN, "CANCELAR");

        assertThat(resultado).isEqualTo(ResultadoCancelacion.FUERA_DE_VENTANA);
        assertThat(cita.getEstado()).isEqualTo(EstadoCita.RECORDATORIO_ENVIADO);
        verify(citaRepositoryPort, never()).guardar(any());
        verify(whatsAppGatewayPort).enviarRechazoPorVentanaVencida(paciente);
    }

    @Test
    void dadoUnPacienteConVariasCitasActivas_cuandoResponteEnUnHiloEspecifico_entoncesSoloSeCancelaLaCitaDeEseHilo() {
        Cita citaDelHilo = citaConRecordatorioEnviado(Instant.now().plus(5, ChronoUnit.HOURS));
        Cita otraCitaActiva = Cita.agendar(UUID.randomUUID(), paciente, medico, Instant.now().plus(30, ChronoUnit.HOURS));
        otraCitaActiva.registrarEnvioRecordatorio(Instant.now(), "wamid.otro.hilo");

        when(citaRepositoryPort.buscarPorMensajeWhatsappId(MENSAJE_ORIGEN)).thenReturn(Optional.of(citaDelHilo));

        ResultadoCancelacion resultado = useCase.procesarRespuesta(MENSAJE_ORIGEN, "CANCELAR");

        assertThat(resultado).isEqualTo(ResultadoCancelacion.CANCELADA);
        assertThat(citaDelHilo.getEstado()).isEqualTo(EstadoCita.CANCELADA);
        assertThat(otraCitaActiva.getEstado()).isEqualTo(EstadoCita.RECORDATORIO_ENVIADO);
        verify(citaRepositoryPort, never()).buscarPorMensajeWhatsappId("wamid.otro.hilo");
    }

    @Test
    void dadoUnMensajeSinHiloAsociado_cuandoSeProcesa_entoncesNoSeModificaNadaYSeDevuelveHiloNoEncontrado() {
        when(citaRepositoryPort.buscarPorMensajeWhatsappId("wamid.desconocido")).thenReturn(Optional.empty());

        ResultadoCancelacion resultado = useCase.procesarRespuesta("wamid.desconocido", "CANCELAR");

        assertThat(resultado).isEqualTo(ResultadoCancelacion.HILO_NO_ENCONTRADO);
        verifyNoInteractions(whatsAppGatewayPort);
    }
}
