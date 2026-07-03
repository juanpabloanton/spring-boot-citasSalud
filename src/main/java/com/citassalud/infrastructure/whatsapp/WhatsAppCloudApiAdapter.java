package com.citassalud.infrastructure.whatsapp;

import com.citassalud.application.port.WhatsAppGatewayPort;
import com.citassalud.domain.cita.Cita;
import com.citassalud.domain.medico.Medico;
import com.citassalud.domain.paciente.Paciente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Adaptador HTTP hacia WhatsApp Cloud API de Meta (research.md §1).
 * Reintenta hasta 3 veces con espaciamiento creciente ante fallos temporales (5xx/conexión)
 * del proveedor, según las Suposiciones de spec.md. Los errores del cliente (4xx, por ejemplo
 * un número de destino inválido) se consideran permanentes y no se reintentan.
 */
@Component
public class WhatsAppCloudApiAdapter implements WhatsAppGatewayPort {

    private static final Logger LOG = LoggerFactory.getLogger(WhatsAppCloudApiAdapter.class);
    private static final int MAX_INTENTOS = 3;
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(Locale.of("es", "CO"));

    private final RestClient restClient;
    private final String numeroTelefonoId;
    private final long espaciamientoBaseMs;

    public WhatsAppCloudApiAdapter(RestClient.Builder restClientBuilder,
                                    @Value("${whatsapp.cloud-api.base-url}") String baseUrl,
                                    @Value("${whatsapp.cloud-api.numero-telefono-id}") String numeroTelefonoId,
                                    @Value("${whatsapp.cloud-api.token-acceso}") String tokenAcceso,
                                    @Value("${whatsapp.cloud-api.reintento.espaciamiento-base-ms:2000}") long espaciamientoBaseMs) {
        HttpClient jdkHttpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(jdkHttpClient))
                .defaultHeader("Authorization", "Bearer " + tokenAcceso)
                .build();
        this.numeroTelefonoId = numeroTelefonoId;
        this.espaciamientoBaseMs = espaciamientoBaseMs;
    }

    @Override
    public ResultadoEnvioWhatsApp enviarRecordatorio(Paciente paciente, Medico medico, Cita cita) {
        String texto = construirMensajeRecordatorio(paciente, medico, cita);
        return enviarConReintentos(paciente.getNumeroWhatsapp(), texto);
    }

    @Override
    public void enviarConfirmacionCancelacion(Paciente paciente, Cita cita) {
        enviarConReintentos(paciente.getNumeroWhatsapp(),
                "Su cita ha sido cancelada exitosamente. La franja horaria quedó disponible nuevamente.");
    }

    @Override
    public void enviarAclaracion(Paciente paciente) {
        enviarConReintentos(paciente.getNumeroWhatsapp(),
                "No reconocimos su respuesta. Para cancelar su cita, responda únicamente con la palabra CANCELAR.");
    }

    @Override
    public void enviarRechazoPorVentanaVencida(Paciente paciente) {
        enviarConReintentos(paciente.getNumeroWhatsapp(),
                "Ya no es posible cancelar esta cita por este canal. Por favor, contacte directamente al centro de salud.");
    }

    private String construirMensajeRecordatorio(Paciente paciente, Medico medico, Cita cita) {
        String fechaHora = FORMATO_FECHA_HORA.format(cita.getFechaHora().atZone(ZoneId.systemDefault()));
        return "Hola %s, le recordamos su cita el %s con %s.".formatted(paciente.getNombre(), fechaHora, medico.getNombre());
    }

    private ResultadoEnvioWhatsApp enviarConReintentos(String numeroDestino, String texto) {
        EnvioMensajeRequest cuerpo = EnvioMensajeRequest.deTexto(numeroDestino, texto);
        int intentos = 0;
        while (intentos < MAX_INTENTOS) {
            intentos++;
            try {
                EnvioMensajeResponse respuesta = restClient.post()
                        .uri("/{numeroTelefonoId}/messages", numeroTelefonoId)
                        .body(cuerpo)
                        .retrieve()
                        .body(EnvioMensajeResponse.class);
                String mensajeId = respuesta != null ? respuesta.primerMensajeId() : null;
                return new ResultadoEnvioWhatsApp(true, mensajeId, intentos);
            } catch (RestClientResponseException ex) {
                if (!ex.getStatusCode().is5xxServerError() || intentos >= MAX_INTENTOS) {
                    LOG.warn("Fallo permanente al enviar WhatsApp a {} tras {} intento(s): {}",
                            numeroDestino, intentos, ex.getMessage());
                    return new ResultadoEnvioWhatsApp(false, null, intentos);
                }
                esperarAntesDelSiguienteIntento(intentos);
            } catch (Exception ex) {
                if (intentos >= MAX_INTENTOS) {
                    LOG.warn("Fallo de conexión al enviar WhatsApp a {} tras {} intento(s): {}",
                            numeroDestino, intentos, ex.getMessage());
                    return new ResultadoEnvioWhatsApp(false, null, intentos);
                }
                esperarAntesDelSiguienteIntento(intentos);
            }
        }
        return new ResultadoEnvioWhatsApp(false, null, intentos);
    }

    private void esperarAntesDelSiguienteIntento(int intentoActual) {
        try {
            Thread.sleep(Duration.ofMillis(espaciamientoBaseMs * intentoActual).toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
