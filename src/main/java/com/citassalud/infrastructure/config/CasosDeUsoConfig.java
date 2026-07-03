package com.citassalud.infrastructure.config;

import com.citassalud.application.port.CitaRepositoryPort;
import com.citassalud.application.port.WhatsAppGatewayPort;
import com.citassalud.application.usecase.CancelarCitaUseCase;
import com.citassalud.application.usecase.EnviarRecordatorioUseCase;
import com.citassalud.domain.cita.VentanaCancelacionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * La capa de aplicación (casos de uso) no depende de Spring (Principio I), por lo que
 * su instanciación como bean se declara aquí, en infraestructura.
 */
@Configuration
public class CasosDeUsoConfig {

    @Bean
    public EnviarRecordatorioUseCase enviarRecordatorioUseCase(CitaRepositoryPort citaRepositoryPort,
                                                                 WhatsAppGatewayPort whatsAppGatewayPort) {
        return new EnviarRecordatorioUseCase(citaRepositoryPort, whatsAppGatewayPort);
    }

    @Bean
    public VentanaCancelacionPolicy ventanaCancelacionPolicy() {
        return new VentanaCancelacionPolicy();
    }

    @Bean
    public CancelarCitaUseCase cancelarCitaUseCase(CitaRepositoryPort citaRepositoryPort,
                                                     WhatsAppGatewayPort whatsAppGatewayPort,
                                                     VentanaCancelacionPolicy ventanaCancelacionPolicy) {
        return new CancelarCitaUseCase(citaRepositoryPort, whatsAppGatewayPort, ventanaCancelacionPolicy);
    }
}
