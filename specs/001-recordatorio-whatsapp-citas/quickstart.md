# Quickstart: Recordatorio de Cita por WhatsApp

## Prerrequisitos

- JDK 25 (toolchain configurado en `build.gradle`)
- Ninguna base de datos externa: H2 embebida se usa para desarrollo y pruebas de integración

## Compilar y ejecutar la suite completa (incluye la puerta de cobertura JaCoCo)

```bash
./gradlew check
```

Esto ejecuta: pruebas unitarias, pruebas de integración, pruebas funcionales (Cucumber), pruebas de arquitectura (ArchUnit), genera el código a partir del contrato OpenAPI, y verifica los umbrales de cobertura JaCoCo (>80% por clase, >=80% global). El build falla si algún umbral no se cumple.

## Ver el reporte de cobertura JaCoCo

Después de `./gradlew check`, el reporte HTML queda en:

```text
build/reports/jacoco/test/html/index.html
```

## Ejecutar solo los escenarios de aceptación (Cucumber/BDD)

```bash
./gradlew test --tests "com.citassalud.functional.*"
```

Los escenarios viven como archivos `.feature` (Gherkin) y reproducen literalmente los "Escenarios de Aceptación" de `spec.md`:

- Historia de Usuario 1: envío del recordatorio 24h antes del turno.
- Historia de Usuario 2: cancelación de la cita respondiendo "CANCELAR" al recordatorio.

## Levantar la aplicación localmente

```bash
./gradlew bootRun
```

La consola H2 queda disponible (perfil de desarrollo) para inspeccionar el estado de `Cita` y `Recordatorio`.

## Probar el flujo manualmente (sin proveedor real de WhatsApp)

1. Crear/agendar una cita con `fechaHora` = ahora + 24 horas (vía el mecanismo de agendamiento existente del sistema, fuera del alcance de esta funcionalidad).
2. Esperar (o disparar manualmente en pruebas) el job `RecordatorioSchedulerJob`; verificar en H2 que la `Cita` pasa a `RECORDATORIO_ENVIADO` y que se creó un `Recordatorio`.
3. Simular la respuesta del paciente enviando un `POST /api/v1/whatsapp/webhook` con un payload como el definido en `contracts/whatsapp-webhook.openapi.yaml`, con `text.body = "CANCELAR"` y `context.id` igual al `mensajeProveedorId` del `Recordatorio` creado en el paso 2.
4. Verificar que la `Cita` pasa a `CANCELADA` y que la franja horaria queda disponible nuevamente.

## Validar el contrato OpenAPI

El contrato fuente vive en `specs/001-recordatorio-whatsapp-citas/contracts/whatsapp-webhook.openapi.yaml`; su copia versionada para el build está en `src/main/resources/openapi/whatsapp-webhook.yaml`. Cualquier cambio de comportamiento del webhook debe actualizar primero este contrato (Principio IV de la constitución) antes de tocar `WhatsAppWebhookController`.
