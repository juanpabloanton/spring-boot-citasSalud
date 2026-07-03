package com.citassalud.infrastructure.whatsapp;

record EnvioMensajeRequest(String messaging_product, String to, String type, TextoMensaje text) {

    static EnvioMensajeRequest deTexto(String numeroDestino, String cuerpo) {
        return new EnvioMensajeRequest("whatsapp", numeroDestino, "text", new TextoMensaje(cuerpo));
    }

    record TextoMensaje(String body) {
    }
}
