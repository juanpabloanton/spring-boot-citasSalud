package com.citassalud.integration;

import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.cita.EstadoCita;
import com.citassalud.domain.medico.Medico;
import com.citassalud.domain.paciente.Paciente;
import com.citassalud.infrastructure.persistence.CitaJpaRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CitaJpaRepositoryAdapter.class)
class CitaJpaRepositoryAdapterIT {

    @Autowired
    private CitaJpaRepositoryAdapter adapter;

    @Test
    void dadaUnaCitaAgendada_cuandoSeGuardaYSeBuscaPorId_entoncesSeRecuperaConLosMismosDatos() {
        Medico medico = new Medico(UUID.randomUUID(), "Dra. Ana Torres");
        Paciente paciente = new Paciente(UUID.randomUUID(), "Juan Pérez", "573001234567");
        Cita cita = Cita.agendar(UUID.randomUUID(), paciente, medico, Instant.now().plus(24, ChronoUnit.HOURS));

        adapter.guardar(cita);

        Optional<Cita> recuperada = adapter.buscarPorId(cita.getId());

        assertThat(recuperada).isPresent();
        assertThat(recuperada.get().getEstado()).isEqualTo(EstadoCita.AGENDADA);
        assertThat(recuperada.get().getPaciente().getNombre()).isEqualTo("Juan Pérez");
        assertThat(recuperada.get().getMedico().getNombre()).isEqualTo("Dra. Ana Torres");
    }

    @Test
    void dadasCitasEnDistintasVentanas_cuandoSeBuscaLaVentanaDe24Horas_entoncesSoloDevuelveLasQueCaenDentro() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Luis Rey");
        Paciente paciente = new Paciente(UUID.randomUUID(), "María Ruiz", "573009876543");

        Instant ahora = Instant.now();
        Cita dentroDeVentana = Cita.agendar(UUID.randomUUID(), paciente, medico, ahora.plus(24, ChronoUnit.HOURS));
        Cita fueraDeVentana = Cita.agendar(UUID.randomUUID(), paciente, medico, ahora.plus(48, ChronoUnit.HOURS));

        adapter.guardar(dentroDeVentana);
        adapter.guardar(fueraDeVentana);

        List<Cita> resultado = adapter.buscarAgendadasEnVentana(
                ahora.plus(23, ChronoUnit.HOURS), ahora.plus(25, ChronoUnit.HOURS));

        assertThat(resultado).extracting(Cita::getId).containsExactly(dentroDeVentana.getId());
    }

    @Test
    void dadaUnaCitaCanceladaDentroDeVentana_cuandoSeBuscaLaVentanaDe24Horas_entoncesNoSeDevuelve() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Luis Rey");
        Paciente paciente = new Paciente(UUID.randomUUID(), "María Ruiz", "573009876543");

        Instant ahora = Instant.now();
        Cita citaAgendada = Cita.agendar(UUID.randomUUID(), paciente, medico, ahora.plus(24, ChronoUnit.HOURS));
        Cita citaCancelada = Cita.agendar(UUID.randomUUID(), paciente, medico, ahora.plus(24, ChronoUnit.HOURS));
        citaCancelada.cancelar();

        adapter.guardar(citaAgendada);
        adapter.guardar(citaCancelada);

        List<Cita> resultado = adapter.buscarAgendadasEnVentana(
                ahora.plus(23, ChronoUnit.HOURS), ahora.plus(25, ChronoUnit.HOURS));

        assertThat(resultado).extracting(Cita::getId).containsExactly(citaAgendada.getId());
    }

    @Test
    void dadaUnaCitaConRecordatorioEnviado_cuandoSeBuscaPorMensajeWhatsappId_entoncesSeResuelveLaCitaOrigen() {
        Medico medico = new Medico(UUID.randomUUID(), "Dra. Ana Torres");
        Paciente paciente = new Paciente(UUID.randomUUID(), "Juan Pérez", "573001234567");
        Cita cita = Cita.agendar(UUID.randomUUID(), paciente, medico, Instant.now().plus(24, ChronoUnit.HOURS));
        cita.registrarEnvioRecordatorio(Instant.now(), "wamid.abc123");
        adapter.guardar(cita);

        Optional<Cita> resuelta = adapter.buscarPorMensajeWhatsappId("wamid.abc123");

        assertThat(resuelta).isPresent();
        assertThat(resuelta.get().getId()).isEqualTo(cita.getId());
    }
}
