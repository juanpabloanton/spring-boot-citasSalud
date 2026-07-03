# Investigación (Fase 0): Recordatorio de Cita por WhatsApp

## 1. Proveedor de mensajería de WhatsApp

**Decisión**: WhatsApp Cloud API de Meta (oficial), integrada mediante un adaptador HTTP propio detrás del puerto `WhatsAppGatewayPort`.

**Justificación**: Es la API oficial de Meta para WhatsApp Business, no añade una capa de facturación de un tercero (a diferencia de Twilio u otros BSP), tiene un nivel gratuito de conversaciones y soporta tanto el envío de plantillas de mensaje (necesario para el recordatorio) como la recepción de mensajes entrantes vía webhook (necesario para la cancelación). Al aislarla detrás de `WhatsAppGatewayPort` (Principio I y III de la constitución — Inversión de Dependencias), el proveedor concreto queda intercambiable sin tocar `domain` ni `application`.

**Alternativas consideradas**:
- **Twilio WhatsApp API**: más simple de integrar y con mejor documentación para prototipos, pero añade un costo/markup adicional por mensaje y una dependencia de un tercero sobre otro tercero (Twilio sobre Meta). Descartada por costo operativo adicional no justificado para el alcance actual.
- **Proveedores BSP locales**: variabilidad de calidad y documentación; se descartan por falta de un candidato concreto validado por el negocio.

## 2. Mecanismo de disparo del recordatorio (24h antes del turno)

**Decisión**: Job periódico con Spring `@Scheduled(fixedRate = ...)` (por ejemplo, cada 5 minutos) que consulta las citas cuyo horario cae dentro de la ventana `[ahora + 24h, ahora + 24h + margen]` y no tienen recordatorio enviado, y dispara `EnviarRecordatorioUseCase` para cada una.

**Justificación**: Un poller periódico es idempotente (se puede volver a ejecutar sin duplicar envíos si se marca el estado `RECORDATORIO_ENVIADO` de forma atómica) y resiliente a reinicios del proceso, a diferencia de programar una tarea dinámica por cita (`ScheduledExecutorService` en memoria), que se perdería si la aplicación se reinicia. Encaja con el objetivo de SC-001 (95% de recordatorios dentro de ±15 min) usando un intervalo de sondeo de 5 minutos.

**Alternativas consideradas**:
- **Tarea dinámica por cita programada en memoria al crear la cita**: más precisa en el instante exacto, pero se pierde ante un reinicio o despliegue, y añade complejidad de gestión de tareas cancelables. Descartada por menor resiliencia operativa.
- **Cron externo / Quartz**: añade una dependencia y complejidad operativa adicional no justificada por el volumen de este proyecto (YAGNI, Principio III).

## 3. Framework BDD para pruebas funcionales/de aceptación

**Decisión**: Cucumber-JVM sobre JUnit 5 Platform, con escenarios `.feature` en Gherkin que reflejan literalmente los "Escenarios de Aceptación" (Dado/Cuando/Entonces) de `spec.md`.

**Justificación**: Es el estándar de facto para BDD en el ecosistema Java/Spring, se integra de forma nativa con `junit-platform-launcher` (ya presente en `build.gradle`), y permite que los escenarios Gherkin sirvan como especificación ejecutable exigida por el Principio II de la constitución.

**Alternativas consideradas**:
- **JUnit 5 puro con nombres de test estilo Given/When/Then**: más simple de configurar, pero no produce un artefacto legible por no-técnicos (los archivos `.feature` sí lo son) y se aleja del espíritu explícito de "BDD" pedido en la constitución.
- **JBehave**: alternativa BDD más antigua, con menor adopción y soporte actual que Cucumber-JVM. Descartada.

## 4. Persistencia

**Decisión**: Spring Data JPA sobre H2 (ya presente en el proyecto como `runtimeOnly com.h2database:h2`), añadiendo la dependencia `spring-boot-starter-data-jpa`.

**Justificación**: H2 ya está configurado en el proyecto (contexto académico/desarrollo) y Spring Data JPA es el estándar de Spring Boot para persistencia, con soporte directo para pruebas de integración embebidas. El repositorio de dominio se expone como `CitaRepositoryPort` (puerto de `application`), implementado por un adaptador JPA en `infrastructure`, preservando la Regla de Dependencia del Principio I.

**Alternativas consideradas**:
- **JDBC plano / MyBatis**: menor "magia" pero más código repetitivo (viola DRY) sin beneficio claro para el alcance de esta funcionalidad. Descartada.

## 5. Verificación de límites de arquitectura

**Decisión**: ArchUnit, con una regla que verifica que ninguna clase de `domain` dependa de `org.springframework.*`, `jakarta.persistence.*` ni de paquetes de `infrastructure`/`interfaces`.

**Justificación**: El Principio I de la constitución exige, "cuando sea factible", reforzar los límites de capas con pruebas de arquitectura automatizadas además de la revisión manual. ArchUnit es el estándar de facto en el ecosistema Java para este propósito y se ejecuta como parte de la suite de pruebas normal (sin infraestructura adicional).

## 6. Generación de contrato OpenAPI

**Decisión**: Plugin Gradle `org.openapi.generator` apuntando a `src/main/resources/openapi/whatsapp-webhook.yaml`, generando las interfaces del controlador Spring (`interfaceOnly=true`) en tiempo de build; `WhatsAppWebhookController` implementa la interfaz generada y contiene solo la lógica de invocación al caso de uso.

**Justificación**: Cumple directamente el Principio IV (API First) y la restricción de tecnología de la constitución, que exige `openapi-generator` integrado en el build de Gradle en lugar de controladores escritos a mano desde cero.

## 7. Cobertura JaCoCo

**Decisión**: Plugin Gradle `jacoco` con una tarea `jacocoTestCoverageVerification` ligada a la tarea `check`, con reglas `element = CLASS, minimum = 0.80` (por clase) y una regla adicional a nivel de `BUNDLE` con `minimum = 0.80` (global), y `jacocoTestReport` configurado para generar HTML y XML.

**Justificación**: Traduce directamente el Principio V de la constitución (cobertura > 80% por clase, >= 80% global, build que falla si no se cumple) a configuración de Gradle verificable en CI.

## Resumen de NEEDS CLARIFICATION resueltos

Todos los `NEEDS CLARIFICATION` de negocio ya habían sido resueltos en `spec.md` durante la sesión de `/speckit-clarify` (2026-07-03). Los puntos técnicos abiertos en el Contexto Técnico de `plan.md` (proveedor de WhatsApp, mecanismo de scheduling, framework BDD, persistencia, verificación de arquitectura, generación OpenAPI, configuración JaCoCo) quedan resueltos en las secciones 1–7 anteriores. No quedan marcadores pendientes.
