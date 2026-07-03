package com.citassalud.domain.medico;

import java.util.Objects;
import java.util.UUID;

public class Medico {

    private final UUID id;
    private final String nombre;

    public Medico(UUID id, String nombre) {
        this.id = Objects.requireNonNull(id, "id no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre, "nombre no puede ser nulo");
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Medico medico)) return false;
        return id.equals(medico.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
