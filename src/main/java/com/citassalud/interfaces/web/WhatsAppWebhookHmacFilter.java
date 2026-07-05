package com.citassalud.interfaces.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class WhatsAppWebhookHmacFilter extends OncePerRequestFilter {

    private static final String WEBHOOK_URI = "/api/v1/whatsapp/webhook";

    private final String appSecret;

    public WhatsAppWebhookHmacFilter(@Value("${whatsapp.webhook.app-secret:}") String appSecret) {
        this.appSecret = appSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if (!esPostAlWebhook(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (appSecret.isEmpty()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Webhook no configurado: falta whatsapp.webhook.app-secret");
            return;
        }

        String firma = request.getHeader("X-Hub-Signature-256");
        if (firma == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Falta cabecera X-Hub-Signature-256");
            return;
        }

        byte[] cuerpo = request.getInputStream().readAllBytes();

        if (!firmaValida(cuerpo, firma)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Firma X-Hub-Signature-256 invalida");
            return;
        }

        filterChain.doFilter(new SolicitudConCuerpoReenviable(request, cuerpo), response);
    }

    private boolean esPostAlWebhook(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && WEBHOOK_URI.equals(request.getRequestURI());
    }

    private boolean firmaValida(byte[] cuerpo, String firma) {
        try {
            if (!firma.startsWith("sha256=")) {
                return false;
            }
            String hexRecibido = firma.substring(7);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String hexComputado = HexFormat.of().formatHex(mac.doFinal(cuerpo));
            return MessageDigest.isEqual(
                    hexComputado.getBytes(StandardCharsets.UTF_8),
                    hexRecibido.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    private static class SolicitudConCuerpoReenviable extends HttpServletRequestWrapper {

        private final byte[] cuerpo;

        SolicitudConCuerpoReenviable(HttpServletRequest request, byte[] cuerpo) {
            super(request);
            this.cuerpo = cuerpo;
        }

        @Override
        public ServletInputStream getInputStream() {
            return new EntradaServletDesdeBytesArray(cuerpo);
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }

    private static class EntradaServletDesdeBytesArray extends ServletInputStream {

        private final ByteArrayInputStream flujo;

        EntradaServletDesdeBytesArray(byte[] cuerpo) {
            this.flujo = new ByteArrayInputStream(cuerpo);
        }

        @Override
        public boolean isFinished() {
            return flujo.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() throws IOException {
            return flujo.read();
        }
    }
}
