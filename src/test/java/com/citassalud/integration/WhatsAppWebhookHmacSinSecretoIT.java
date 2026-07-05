package com.citassalud.integration;

import com.citassalud.application.usecase.CancelarCitaUseCase;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FR-006/hallazgo de seguridad high: cubre el camino fail-closed cuando
 * whatsapp.webhook.app-secret NO está configurado (antes el filtro dejaba pasar
 * el POST sin validar firma; ahora debe rechazarlo con 403 aunque el payload
 * traiga una firma X-Hub-Signature-256 bien formada).
 */
@WebMvcTest(WhatsAppWebhookController.class)
@TestPropertySource(properties = {
        "whatsapp.webhook.token-verificacion=token-secreto",
        "whatsapp.webhook.app-secret="
})
class WhatsAppWebhookHmacSinSecretoIT {

    private static final String PAYLOAD = "{\"object\":\"whatsapp_business_account\",\"entry\":[]}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CancelarCitaUseCase cancelarCitaUseCase;

    @Test
    void dadoAppSecretVacio_cuandoLlegaUnPostAlWebhookInclusoConFirmaBienFormada_entoncesRechazaConForbidden()
            throws Exception {
        String firma = computarFirmaHmac("cualquier-valor", PAYLOAD);

        mockMvc.perform(post("/api/v1/whatsapp/webhook")
                        .contentType("application/json")
                        .header("X-Hub-Signature-256", firma)
                        .content(PAYLOAD))
                .andExpect(status().isForbidden());
    }

    @Test
    void dadoAppSecretVacio_cuandoLlegaUnPostAlWebhookSinFirma_entoncesRechazaConForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/whatsapp/webhook")
                        .contentType("application/json")
                        .content(PAYLOAD))
                .andExpect(status().isForbidden());
    }

    private static String computarFirmaHmac(String secret, String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return "sha256=" + HexFormat.of().formatHex(hash);
    }
}
