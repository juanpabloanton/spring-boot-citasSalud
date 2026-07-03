package com.citassalud.domain.paciente;

import java.util.Objects;
import java.util.UUID;

public class Paciente {

    private final UUID id;
    private final String nombre;
    private final String numeroWhatsapp;

    public Paciente(UUID id, String nombre, String numeroWhatsapp) {
        this.id = Objects.requireNonNull(id, "id no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre, "nombre no puede ser nulo");
        this.numeroWhatsapp = numeroWhatsapp;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNumeroWhatsapp() {
        return numeroWhatsapp;
    }

    public boolean tieneNumeroWhatsappValido() {
        return numeroWhatsapp != null && numeroWhatsapp.matches("^\\d{8,15}$");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Paciente paciente)) return false;
        return id.equals(paciente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
