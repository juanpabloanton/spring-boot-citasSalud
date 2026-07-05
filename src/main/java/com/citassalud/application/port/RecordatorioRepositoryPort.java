package com.citassalud.application.port;

import com.citassalud.domain.recordatorio.Recordatorio;

/**
 * FR-006: puerto de persistencia para dejar constancia auditable de cada intento
 * de envío de recordatorio (enviado, fallido, sin número válido).
 */
public interface RecordatorioRepositoryPort {

    Recordatorio guardar(Recordatorio recordatorio);
}
