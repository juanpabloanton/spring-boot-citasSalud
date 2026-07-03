package com.citassalud.interfaces.web;

import com.citassalud.application.usecase.CancelarCitaUseCase;
import com.citassalud.interfaces.web.generated.api.DefaultApi;
import com.citassalud.interfaces.web.generated.model.WebhookAck;
import com.citassalud.interfaces.web.generated.model.WhatsAppEntry;
import com.citassalud.interfaces.web.generated.model.WhatsAppInboundMessage;
import com.citassalud.interfaces.web.generated.model.WhatsAppWebhookEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/whatsapp")
public class WhatsAppWebhookController implements DefaultApi {

    private final CancelarCitaUseCase cancelarCitaUseCase;
    private final String tokenVerificacionWebhook;

    public WhatsAppWebhookController(CancelarCitaUseCase cancelarCitaUseCase,
                                      @Value("${whatsapp.webhook.token-verificacion}") String tokenVerificacionWebhook) {
        this.cancelarCitaUseCase = cancelarCitaUseCase;
        this.tokenVerificacionWebhook = tokenVerificacionWebhook;
    }

    @Override
    public ResponseEntity<String> verificarWebhook(String hubMode, String hubVerifyToken, String hubChallenge) {
        if ("subscribe".equals(hubMode) && tokenVerificacionWebhook.equals(hubVerifyToken)) {
            return ResponseEntity.ok(hubChallenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Override
    public ResponseEntity<WebhookAck> recibirMensajeWhatsapp(WhatsAppWebhookEvent evento) {
        extraerMensajeEntrante(evento).ifPresent(mensaje -> {
            String mensajeOrigenId = mensaje.getContext() != null ? mensaje.getContext().getId() : null;
            String texto = mensaje.getText() != null ? mensaje.getText().getBody() : null;
            if (mensajeOrigenId != null) {
                cancelarCitaUseCase.procesarRespuesta(mensajeOrigenId, texto);
            }
        });
        return ResponseEntity.ok(new WebhookAck().status(WebhookAck.StatusEnum.RECIBIDO));
    }

    private Optional<WhatsAppInboundMessage> extraerMensajeEntrante(WhatsAppWebhookEvent evento) {
        return Optional.ofNullable(evento.getEntry())
                .orElse(List.of())
                .stream()
                .map(WhatsAppEntry::getChanges)
                .filter(cambios -> cambios != null)
                .flatMap(List::stream)
                .map(cambio -> cambio.getValue())
                .filter(valor -> valor != null && valor.getMessages() != null)
                .flatMap(valor -> valor.getMessages().stream())
                .findFirst();
    }
}
