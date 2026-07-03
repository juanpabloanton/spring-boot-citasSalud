package com.citassalud.infrastructure.whatsapp;

import java.util.List;

record EnvioMensajeResponse(List<MensajeEnviado> messages) {

    String primerMensajeId() {
        return messages != null && !messages.isEmpty() ? messages.get(0).id() : null;
    }

    record MensajeEnviado(String id) {
    }
}
