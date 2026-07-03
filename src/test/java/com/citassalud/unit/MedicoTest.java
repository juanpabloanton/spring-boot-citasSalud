package com.citassalud.unit;

import com.citassalud.domain.medico.Medico;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MedicoTest {

    @Test
    void dadosDosMedicosConElMismoId_cuandoSeComparan_entoncesSonIgualesYTienenElMismoHash() {
        UUID id = UUID.randomUUID();
        Medico medico1 = new Medico(id, "Dra. Ana Torres");
        Medico medico2 = new Medico(id, "Dra. Ana Torres (actualizado)");

        assertThat(medico1).isEqualTo(medico2);
        assertThat(medico1).hasSameHashCodeAs(medico2);
        assertThat(medico1.getId()).isEqualTo(id);
        assertThat(medico1.getNombre()).isEqualTo("Dra. Ana Torres");
    }

    @Test
    void dadosDosMedicosConIdDistinto_cuandoSeComparan_entoncesNoSonIguales() {
        Medico medico1 = new Medico(UUID.randomUUID(), "Dra. Ana Torres");
        Medico medico2 = new Medico(UUID.randomUUID(), "Dra. Ana Torres");

        assertThat(medico1).isNotEqualTo(medico2);
        assertThat(medico1).isNotEqualTo("no es un Medico");
        assertThat(medico1).isEqualTo(medico1);
    }
}
