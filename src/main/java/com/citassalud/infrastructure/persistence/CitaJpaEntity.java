package com.citassalud.infrastructure.persistence;

import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.cita.EstadoCita;
import com.citassalud.domain.medico.Medico;
import com.citassalud.domain.paciente.Paciente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cita")
public class CitaJpaEntity {

    @Id
    private UUID id;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "paciente_nombre", nullable = false)
    private String pacienteNombre;

    @Column(name = "paciente_numero_whatsapp")
    private String pacienteNumeroWhatsapp;

    @Column(name = "medico_id", nullable = false)
    private UUID medicoId;

    @Column(name = "medico_nombre", nullable = false)
    private String medicoNombre;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCita estado;

    @Column(name = "recordatorio_enviado_en")
    private Instant recordatorioEnviadoEn;

    @Column(name = "ultimo_mensaje_whatsapp_id")
    private String ultimoMensajeWhatsappId;

    protected CitaJpaEntity() {
        // requerido por JPA
    }

    public static CitaJpaEntity desdeDominio(Cita cita) {
        CitaJpaEntity entity = new CitaJpaEntity();
        entity.id = cita.getId();
        entity.pacienteId = cita.getPaciente().getId();
        entity.pacienteNombre = cita.getPaciente().getNombre();
        entity.pacienteNumeroWhatsapp = cita.getPaciente().getNumeroWhatsapp();
        entity.medicoId = cita.getMedico().getId();
        entity.medicoNombre = cita.getMedico().getNombre();
        entity.fechaHora = cita.getFechaHora();
        entity.estado = cita.getEstado();
        entity.recordatorioEnviadoEn = cita.getRecordatorioEnviadoEn();
        entity.ultimoMensajeWhatsappId = cita.getUltimoMensajeWhatsappId();
        return entity;
    }

    public Cita aDominio() {
        Paciente paciente = new Paciente(pacienteId, pacienteNombre, pacienteNumeroWhatsapp);
        Medico medico = new Medico(medicoId, medicoNombre);
        return new Cita(id, paciente, medico, fechaHora, estado, recordatorioEnviadoEn, ultimoMensajeWhatsappId);
    }

    public UUID getId() {
        return id;
    }
}
