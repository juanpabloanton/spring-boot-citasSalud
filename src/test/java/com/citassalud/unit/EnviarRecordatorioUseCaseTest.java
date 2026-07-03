package com.citassalud.unit;

import com.citassalud.application.port.CitaRepositoryPort;
import com.citassalud.application.port.WhatsAppGatewayPort;
import com.citassalud.application.port.WhatsAppGatewayPort.ResultadoEnvioWhatsApp;
import com.citassalud.application.usecase.EnviarRecordatorioUseCase;
import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.cita.EstadoCita;
import com.citassalud.domain.medico.Medico;
import com.citassalud.domain.paciente.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnviarRecordatorioUseCaseTest {

    private CitaRepositoryPort citaRepositoryPort;
    private WhatsAppGatewayPort whatsAppGatewayPort;
    private EnviarRecordatorioUseCase useCase;

    @BeforeEach
    void configurar() {
        citaRepositoryPort = mock(CitaRepositoryPort.class);
        whatsAppGatewayPort = mock(WhatsAppGatewayPort.class);
        useCase = new EnviarRecordatorioUseCase(citaRepositoryPort, whatsAppGatewayPort);
    }

    @Test
    void dadoUnaCitaAgendadaConNumeroValido_cuandoSeEjecutaElEnvio_entoncesEnviaElRecordatorioYMarcaLaCita() {
        Medico medico = new Medico(UUID.randomUUID(), "Dra. Ana Torres");
        Paciente paciente = new Paciente(UUID.randomUUID(), "Juan Pérez", "573001234567");
        Cita cita = Cita.agendar(UUID.randomUUID(), paciente, medico, Instant.now().plus(24, ChronoUnit.HOURS));

        when(citaRepositoryPort.buscarPorId(cita.getId())).thenReturn(Optional.of(cita));
        when(whatsAppGatewayPort.enviarRecordatorio(paciente, medico, cita))
                .thenReturn(new ResultadoEnvioWhatsApp(true, "wamid.123", 1));

        useCase.ejecutar(cita.getId());

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.RECORDATORIO_ENVIADO);
        assertThat(cita.getUltimoMensajeWhatsappId()).isEqualTo("wamid.123");
        verify(citaRepositoryPort).guardar(cita);
    }

    @Test
    void dadoUnPacienteSinNumeroWhatsappValido_cuandoSeEjecutaElEnvio_entoncesRegistraElFalloYNoEnviaMensaje() {
        Medico medico = new Medico(UUID.randomUUID(), "Dra. Ana Torres");
        Paciente paciente = new Paciente(UUID.randomUUID(), "Juan Pérez", null);
        Cita cita = Cita.agendar(UUID.randomUUID(), paciente, medico, Instant.now().plus(24, ChronoUnit.HOURS));

        when(citaRepositoryPort.buscarPorId(cita.getId())).thenReturn(Optional.of(cita));

        useCase.ejecutar(cita.getId());

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.AGENDADA);
        verify(whatsAppGatewayPort, never()).enviarRecordatorio(any(), any(), any());
        verify(citaRepositoryPort, never()).guardar(any());
    }

    @Test
    void dadoUnFalloDelProveedorDeWhatsapp_cuandoSeEjecutaElEnvio_entoncesLaCitaPermaneceAgendada() {
        Medico medico = new Medico(UUID.randomUUID(), "Dra. Ana Torres");
        Paciente paciente = new Paciente(UUID.randomUUID(), "Juan Pérez", "573001234567");
        Cita cita = Cita.agendar(UUID.randomUUID(), paciente, medico, Instant.now().plus(24, ChronoUnit.HOURS));

        when(citaRepositoryPort.buscarPorId(cita.getId())).thenReturn(Optional.of(cita));
        when(whatsAppGatewayPort.enviarRecordatorio(paciente, medico, cita))
                .thenReturn(new ResultadoEnvioWhatsApp(false, null, 3));

        useCase.ejecutar(cita.getId());

        assertThat(cita.getEstado()).isEqualTo(EstadoCita.AGENDADA);
        verify(citaRepositoryPort, never()).guardar(any());
    }

    @Test
    void dadoVariasCitasAgendadas_cuandoSeEjecutaElEnvioParaCadaUna_entoncesCadaUnaRecibeUnRecordatorioIndependiente() {
        Medico medico = new Medico(UUID.randomUUID(), "Dra. Ana Torres");
        Paciente paciente1 = new Paciente(UUID.randomUUID(), "Juan Pérez", "573001234567");
        Paciente paciente2 = new Paciente(UUID.randomUUID(), "Ana Gómez", "573007654321");
        Cita cita1 = Cita.agendar(UUID.randomUUID(), paciente1, medico, Instant.now().plus(24, ChronoUnit.HOURS));
        Cita cita2 = Cita.agendar(UUID.randomUUID(), paciente2, medico, Instant.now().plus(24, ChronoUnit.HOURS));

        when(citaRepositoryPort.buscarPorId(cita1.getId())).thenReturn(Optional.of(cita1));
        when(citaRepositoryPort.buscarPorId(cita2.getId())).thenReturn(Optional.of(cita2));
        when(whatsAppGatewayPort.enviarRecordatorio(eq(paciente1), eq(medico), eq(cita1)))
                .thenReturn(new ResultadoEnvioWhatsApp(true, "wamid.1", 1));
        when(whatsAppGatewayPort.enviarRecordatorio(eq(paciente2), eq(medico), eq(cita2)))
                .thenReturn(new ResultadoEnvioWhatsApp(true, "wamid.2", 1));

        useCase.ejecutar(cita1.getId());
        useCase.ejecutar(cita2.getId());

        assertThat(cita1.getEstado()).isEqualTo(EstadoCita.RECORDATORIO_ENVIADO);
        assertThat(cita2.getEstado()).isEqualTo(EstadoCita.RECORDATORIO_ENVIADO);
        ArgumentCaptor<Cita> captor = ArgumentCaptor.forClass(Cita.class);
        verify(citaRepositoryPort, times(2)).guardar(captor.capture());
        assertThat(captor.getAllValues()).containsExactlyInAnyOrder(cita1, cita2);
    }
}
