package com.citassalud.domain.cita;

import com.citassalud.domain.medico.Medico;
import com.citassalud.domain.paciente.Paciente;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Cita {

    private final UUID id;
    private final Paciente paciente;
    private final Medico medico;
    private final Instant fechaHora;
    private EstadoCita estado;
    private Instant recordatorioEnviadoEn;
    private String ultimoMensajeWhatsappId;

    public Cita(UUID id, Paciente paciente, Medico medico, Instant fechaHora, EstadoCita estado,
                Instant recordatorioEnviadoEn, String ultimoMensajeWhatsappId) {
        this.id = Objects.requireNonNull(id, "id no puede ser nulo");
        this.paciente = Objects.requireNonNull(paciente, "paciente no puede ser nulo");
        this.medico = Objects.requireNonNull(medico, "medico no puede ser nulo");
        this.fechaHora = Objects.requireNonNull(fechaHora, "fechaHora no puede ser nula");
        this.estado = Objects.requireNonNull(estado, "estado no puede ser nulo");
        this.recordatorioEnviadoEn = recordatorioEnviadoEn;
        this.ultimoMensajeWhatsappId = ultimoMensajeWhatsappId;
    }

    public static Cita agendar(UUID id, Paciente paciente, Medico medico, Instant fechaHora) {
        return new Cita(id, paciente, medico, fechaHora, EstadoCita.AGENDADA, null, null);
    }

    /**
     * FR-001/FR-007: solo una cita AGENDADA puede recibir recordatorio.
     */
    public void registrarEnvioRecordatorio(Instant momento, String mensajeProveedorId) {
        if (estado != EstadoCita.AGENDADA) {
            throw new IllegalStateException(
                    "Solo una cita AGENDADA puede recibir un recordatorio, estado actual: " + estado);
        }
        this.estado = EstadoCita.RECORDATORIO_ENVIADO;
        this.recordatorioEnviadoEn = Objects.requireNonNull(momento, "momento no puede ser nulo");
        this.ultimoMensajeWhatsappId = mensajeProveedorId;
    }

    /**
     * FR-008: la validación de la ventana mínima de 2h se aplica antes de invocar este método,
     * mediante {@link VentanaCancelacionPolicy}.
     */
    public void cancelar() {
        if (estado == EstadoCita.CANCELADA) {
            return;
        }
        if (estado == EstadoCita.COMPLETADA) {
            throw new IllegalStateException("No se puede cancelar una cita COMPLETADA");
        }
        this.estado = EstadoCita.CANCELADA;
    }

    public UUID getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Medico getMedico() {
        return medico;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public Instant getRecordatorioEnviadoEn() {
        return recordatorioEnviadoEn;
    }

    public String getUltimoMensajeWhatsappId() {
        return ultimoMensajeWhatsappId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cita cita)) return false;
        return id.equals(cita.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
