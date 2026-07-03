package com.citassalud.application.usecase;

/**
 * Resultado de procesar una respuesta entrante de WhatsApp a un recordatorio (FR-003/FR-003a/FR-008).
 */
public enum ResultadoCancelacion {
    CANCELADA,
    NO_RECONOCIDA,
    FUERA_DE_VENTANA,
    HILO_NO_ENCONTRADO
}
