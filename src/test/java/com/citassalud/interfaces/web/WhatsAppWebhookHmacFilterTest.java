package com.citassalud.interfaces.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WhatsAppWebhookHmacFilterTest {

    private static final String APP_SECRET = "test-app-secret";

    @Test
    void dadoUnCuerpoFirmado_cuandoElFiltroLoReenvia_entoncesElInputStreamCumpleElContratoServlet()
            throws Exception {
        byte[] cuerpo = "{\"mensaje\":\"hola\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/whatsapp/webhook");
        request.setContent(cuerpo);
        request.addHeader("X-Hub-Signature-256", computarFirmaHmac(cuerpo));
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        WhatsAppWebhookHmacFilter filtro = new WhatsAppWebhookHmacFilter(APP_SECRET);

        filtro.doFilter(request, response, filterChain);

        var captor = org.mockito.ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), same(response));

        ServletInputStream inputStream = captor.getValue().getInputStream();
        assertThat(inputStream.isReady()).isTrue();
        assertThat(inputStream.isFinished()).isFalse();
        inputStream.setReadListener(mock(ReadListener.class));
        assertThat(inputStream.readAllBytes()).isEqualTo(cuerpo);
        assertThat(inputStream.isFinished()).isTrue();
    }

    private static String computarFirmaHmac(byte[] cuerpo) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(APP_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(cuerpo));
    }
}
