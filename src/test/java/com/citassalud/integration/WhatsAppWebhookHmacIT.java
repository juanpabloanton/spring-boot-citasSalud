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

@WebMvcTest(WhatsAppWebhookController.class)
@TestPropertySource(properties = {
        "whatsapp.webhook.token-verificacion=token-secreto",
        "whatsapp.webhook.app-secret=test-app-secret"
})
class WhatsAppWebhookHmacIT {

    private static final String APP_SECRET = "test-app-secret";
    private static final String PAYLOAD = "{\"object\":\"whatsapp_business_account\",\"entry\":[]}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CancelarCitaUseCase cancelarCitaUseCase;

    @Test
    void dadoUnPayloadConFirmaHmacValida_cuandoLlegaAlWebhook_entoncesDevuelveOk() throws Exception {
        String firma = computarFirmaHmac(APP_SECRET, PAYLOAD);

        mockMvc.perform(post("/api/v1/whatsapp/webhook")
                        .contentType("application/json")
                        .header("X-Hub-Signature-256", firma)
                        .content(PAYLOAD))
                .andExpect(status().isOk());
    }

    @Test
    void dadoUnPayloadConFirmaHmacInvalida_cuandoLlegaAlWebhook_entoncesDevuelve403() throws Exception {
        mockMvc.perform(post("/api/v1/whatsapp/webhook")
                        .contentType("application/json")
                        .header("X-Hub-Signature-256", "sha256=firmainvalida")
                        .content(PAYLOAD))
                .andExpect(status().isForbidden());
    }

    @Test
    void dadoUnPayloadSinCabeceraFirma_cuandoLlegaAlWebhook_entoncesDevuelve403() throws Exception {
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
