# CitasSalud

Backend Spring Boot del sistema de gestión de citas de un centro de salud.

## Funcionalidad: Recordatorio de Cita por WhatsApp

El sistema envía automáticamente un recordatorio por WhatsApp a cada paciente 24 horas
antes de su cita (fecha, hora y nombre de la médica/médico), y permite cancelar la cita
respondiendo la palabra clave `CANCELAR` desde el mismo hilo de WhatsApp del recordatorio
(hasta 2 horas antes del turno), liberando la franja horaria automáticamente.

- Un job periódico (`RecordatorioSchedulerJob`) detecta las citas que entran en la ventana
  de 24h y dispara el envío a través de `WhatsAppGatewayPort` (implementado por
  `WhatsAppCloudApiAdapter`, integrado con WhatsApp Cloud API de Meta).
- Un endpoint webhook (`POST /api/v1/whatsapp/webhook`), definido primero como contrato
  OpenAPI y con el controlador generado por `openapi-generator`, recibe las respuestas
  entrantes de WhatsApp y ejecuta la cancelación (`CancelarCitaUseCase`).
- Toda la lógica de negocio vive en `domain`/`application`, aislada de Spring y del
  proveedor de WhatsApp (Arquitectura Limpia).

Detalles de diseño, decisiones técnicas y modelo de datos en
[`specs/001-recordatorio-whatsapp-citas/`](specs/001-recordatorio-whatsapp-citas/).

## Cómo compilar y probar

```bash
./gradlew check
```

Ejecuta pruebas unitarias, de integración, funcionales (Cucumber/BDD), de arquitectura
(ArchUnit), genera el código a partir del contrato OpenAPI y verifica los umbrales de
cobertura JaCoCo (>80% por clase, >=80% global).

El reporte de cobertura queda en `build/reports/jacoco/test/html/index.html`.

## Cómo ejecutar la aplicación

```bash
./gradlew bootRun
```

Ver [`specs/001-recordatorio-whatsapp-citas/quickstart.md`](specs/001-recordatorio-whatsapp-citas/quickstart.md)
para el flujo manual completo (agendar cita, disparar el recordatorio, cancelar por WhatsApp).
