package com.citassalud.infrastructure.persistence;

import com.citassalud.domain.recordatorio.EstadoEnvioRecordatorio;
import com.citassalud.domain.recordatorio.Recordatorio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recordatorio")
public class RecordatorioJpaEntity {

    @Id
    private UUID id;

    @Column(name = "cita_id", nullable = false)
    private UUID citaId;

    @Column(name = "canal", nullable = false)
    private String canal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_envio", nullable = false)
    private EstadoEnvioRecordatorio estadoEnvio;

    @Column(name = "enviado_en")
    private Instant enviadoEn;

    @Column(name = "intentos", nullable = false)
    private int intentos;

    @Column(name = "mensaje_proveedor_id")
    private String mensajeProveedorId;

    protected RecordatorioJpaEntity() {
        // requerido por JPA
    }

    public static RecordatorioJpaEntity desdeDominio(Recordatorio recordatorio) {
        RecordatorioJpaEntity entity = new RecordatorioJpaEntity();
        entity.id = recordatorio.getId();
        entity.citaId = recordatorio.getCitaId();
        entity.canal = recordatorio.getCanal();
        entity.estadoEnvio = recordatorio.getEstadoEnvio();
        entity.enviadoEn = recordatorio.getEnviadoEn();
        entity.intentos = recordatorio.getIntentos();
        entity.mensajeProveedorId = recordatorio.getMensajeProveedorId();
        return entity;
    }

    public Recordatorio aDominio() {
        return new Recordatorio(id, citaId, canal, estadoEnvio, enviadoEn, intentos, mensajeProveedorId);
    }

    public UUID getId() {
        return id;
    }
}
