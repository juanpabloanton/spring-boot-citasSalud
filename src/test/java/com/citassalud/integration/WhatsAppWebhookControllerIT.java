package com.citassalud.integration;

import com.citassalud.application.usecase.CancelarCitaUseCase;
import com.citassalud.application.usecase.ResultadoCancelacion;
import com.citassalud.interfaces.web.WhatsAppWebhookController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WhatsAppWebhookController.class)
@TestPropertySource(properties = {
        "whatsapp.webhook.token-verificacion=token-secreto",
        "whatsapp.webhook.app-secret=test-app-secret"
})
class WhatsAppWebhookControllerIT {

    private static final String APP_SECRET = "test-app-secret";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CancelarCitaUseCase cancelarCitaUseCase;

    @Test
    void dadoElTokenDeVerificacionCorrecto_cuandoMetaVerificaElWebhook_entoncesDevuelveElChallenge() throws Exception {
        mockMvc.perform(get("/api/v1/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "token-secreto")
                        .param("hub.challenge", "12345"))
                .andExpect(status().isOk())
                .andExpect(content().string("12345"));
    }

    @Test
    void dadoUnTokenDeVerificacionIncorrecto_cuandoMetaVerificaElWebhook_entoncesDevuelve403() throws Exception {
        mockMvc.perform(get("/api/v1/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "token-invalido")
                        .param("hub.challenge", "12345"))
                .andExpect(status().isForbidden());
    }

    @Test
    void dadoUnMensajeEntranteConCancelar_cuandoLlegaAlWebhook_entoncesDelegaEnElCasoDeUsoYRespondeOk() throws Exception {
        when(cancelarCitaUseCase.procesarRespuesta(eq("wamid.origen123"), eq("CANCELAR")))
                .thenReturn(ResultadoCancelacion.CANCELADA);

        String payload = """
                {
                  "object": "whatsapp_business_account",
                  "entry": [
                    {
                      "id": "1",
                      "changes": [
                        {
                          "field": "messages",
                          "value": {
                            "messages": [
                              {
                                "from": "573001234567",
                                "id": "wamid.entrante1",
                                "type": "text",
                                "context": { "id": "wamid.origen123" },
                                "text": { "body": "CANCELAR" }
                              }
                            ]
                          }
                        }
                      ]
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/whatsapp/webhook")
                        .contentType("application/json")
                        .header("X-Hub-Signature-256", computarFirmaHmac(APP_SECRET, payload))
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"recibido\"}"));

        verify(cancelarCitaUseCase).procesarRespuesta("wamid.origen123", "CANCELAR");
    }

    @Test
    void dadoUnPayloadSinMensajes_cuandoLlegaAlWebhook_entoncesNoDelegaEnElCasoDeUsoYRespondeOk() throws Exception {
        String payload = """
                {
                  "object": "whatsapp_business_account",
                  "entry": [ { "id": "1", "changes": [ { "field": "messages", "value": {} } ] } ]
                }
                """;

        mockMvc.perform(post("/api/v1/whatsapp/webhook")
                        .contentType("application/json")
                        .header("X-Hub-Signature-256", computarFirmaHmac(APP_SECRET, payload))
                        .content(payload))
                .andExpect(status().isOk());

        verify(cancelarCitaUseCase, org.mockito.Mockito.never()).procesarRespuesta(any(), any());
    }

    private static String computarFirmaHmac(String secret, String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return "sha256=" + HexFormat.of().formatHex(hash);
    }
}
