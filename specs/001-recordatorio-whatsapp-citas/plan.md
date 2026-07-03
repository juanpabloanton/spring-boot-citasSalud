# Plan de Implementación: Recordatorio de Cita por WhatsApp

**Rama**: `001-recordatorio-whatsapp-citas` | **Fecha**: 2026-07-03 | **Spec**: [spec.md](./spec.md)

**Entrada**: Especificación de funcionalidad desde `/specs/001-recordatorio-whatsapp-citas/spec.md`

**Nota**: Este archivo es generado por el comando `/speckit-plan`. Ver `.specify/templates/plan-template.md` para el flujo de ejecución.

## Resumen

El sistema debe enviar automáticamente un recordatorio por WhatsApp a cada paciente 24 horas antes de su cita (fecha, hora y nombre de la médica/médico), y permitir que el paciente cancele la cita respondiendo la palabra clave "CANCELAR" desde el mismo hilo de WhatsApp del recordatorio (hasta 2 horas antes del turno), liberando la franja horaria automáticamente. El enfoque técnico: un job periódico (Spring `@Scheduled`) detecta citas que entran en la ventana de 24h y dispara el envío vía un puerto `WhatsAppGatewayPort` (Arquitectura Limpia); un endpoint webhook (contrato OpenAPI, generado con `openapi-generator`) recibe las respuestas entrantes de WhatsApp y ejecuta el caso de uso de cancelación. Toda la lógica de negocio vive en `domain`/`application`, aislada de Spring y del proveedor de WhatsApp.

## Contexto Técnico

**Lenguaje/Versión**: Java 25 (toolchain ya configurado en `build.gradle`)

**Dependencias Principales**: Spring Boot 4.1.0 (`spring-boot-starter-webmvc`), Spring Data JPA (a añadir), Spring `@Scheduled` (incluido en Spring core, sin dependencia adicional), Lombok, cliente HTTP del proveedor de WhatsApp (ver `research.md`), plugin Gradle `openapi-generator`, plugin Gradle `jacoco`, Cucumber-JVM + JUnit 5 Platform para BDD funcional (ver `research.md`)

**Almacenamiento**: H2 (ya presente en el proyecto vía `com.h2database:h2`, modo actual de desarrollo/pruebas) con Spring Data JPA

**Pruebas**: JUnit 5 + Mockito (unitarias), Spring Boot Test con H2 embebido (integración), Cucumber-JVM/Gherkin (funcionales/aceptación), ArchUnit (pruebas de arquitectura de capas), JaCoCo (cobertura)

**Plataforma Objetivo**: Servidor Linux/JVM (despliegue backend Spring Boot)

**Tipo de Proyecto**: Servicio web backend (API REST + job programado), proyecto único (no hay frontend en este repositorio)

**Objetivos de Rendimiento**: Envío del recordatorio dentro de la ventana de 24h ± 15 min (SC-001); liberación de la franja horaria en menos de 5 minutos desde la confirmación de cancelación (SC-004)

**Restricciones**: El reconocimiento de cancelación se limita a una palabra clave exacta, sin NLU (Clarifications); la cancelación por WhatsApp solo se acepta hasta 2 horas antes del turno (FR-008); cobertura JaCoCo > 80% por clase y >= 80% global (Constitución, Principio V)

**Escala/Alcance**: Un centro de salud (alcance del proyecto académico), volumen de citas acorde a una clínica pequeña/mediana; sin necesidad de alta concurrencia masiva para esta iteración

## Constitution Check

*GATE: Debe pasar antes de la Fase 0 de investigación. Se vuelve a evaluar después del diseño de la Fase 1.*

| Principio | Evaluación | Estado |
|---|---|---|
| I. Arquitectura Limpia | El plan define capas `domain` (Cita, Paciente, Recordatorio, reglas de cancelación/ventana de 24h y 2h), `application` (casos de uso: `EnviarRecordatorioUseCase`, `CancelarCitaUseCase`), `infrastructure` (adaptador JPA, adaptador HTTP del proveedor de WhatsApp, scheduler), `interfaces` (controlador del webhook generado por OpenAPI). Ninguna clase de dominio depende de Spring/JPA/HTTP. | PASS |
| II. Pruebas BDD (unit/integración/funcional) | Cada historia de usuario (P1 recordatorio, P2 cancelación) tendrá pruebas unitarias de los casos de uso, pruebas de integración del adaptador JPA y del cliente HTTP de WhatsApp (mockeado), y escenarios Cucumber Given/When/Then que reflejan literalmente los criterios de aceptación de la spec. | PASS |
| III. SOLID, YAGNI, DRY | `WhatsAppGatewayPort` es una abstracción definida por `application` e implementada por `infrastructure` (Inversión de Dependencias); no se construye NLU ni reprogramación (fuera de alcance según Suposiciones) — YAGNI; la lógica de "ventana de tiempo válida" se centraliza en un único servicio de dominio reutilizado por ambos casos de uso — DRY. | PASS |
| IV. API First con OpenAPI | El único endpoint expuesto (webhook de recepción de mensajes de WhatsApp) se define primero como contrato OpenAPI 3.x en `contracts/`, y el controlador se genera con `openapi-generator` antes de escribir la lógica de negocio. | PASS |
| V. Cobertura JaCoCo | El plugin JaCoCo se añade a `build.gradle` con una tarea de verificación (`jacocoTestCoverageVerification`) ligada a `check`, con reglas de >80% por clase y >=80% global; se generarán los tests necesarios (Principio II) para cumplirlo. | PASS |

No se identifican violaciones. La sección de Seguimiento de Complejidad no aplica.

**Re-evaluación post-diseño (Fase 1)**: revisados `data-model.md` (capas de dominio explícitas, sin dependencias de framework), `contracts/whatsapp-webhook.openapi.yaml` (contrato definido antes del controlador, único endpoint expuesto) y `research.md` (decisiones de JaCoCo, ArchUnit, openapi-generator y BDD alineadas con la constitución). No se introdujeron desviaciones respecto al chequeo inicial; los cinco principios se mantienen en PASS.

## Estructura del Proyecto

### Documentación (esta funcionalidad)

```text
specs/001-recordatorio-whatsapp-citas/
├── plan.md              # Este archivo (salida de /speckit-plan)
├── research.md          # Salida de la Fase 0 (/speckit-plan)
├── data-model.md         # Salida de la Fase 1 (/speckit-plan)
├── quickstart.md         # Salida de la Fase 1 (/speckit-plan)
├── contracts/             # Salida de la Fase 1 (/speckit-plan)
│   └── whatsapp-webhook.openapi.yaml
└── tasks.md              # Salida de la Fase 2 (/speckit-tasks, NO generado por /speckit-plan)
```

### Código Fuente (raíz del repositorio)

```text
src/main/java/com/citassalud/
├── domain/
│   ├── cita/
│   │   ├── Cita.java
│   │   ├── EstadoCita.java
│   │   └── VentanaCancelacionPolicy.java
│   ├── paciente/
│   │   └── Paciente.java
│   ├── medico/
│   │   └── Medico.java
│   └── recordatorio/
│       ├── Recordatorio.java
│       └── EstadoEnvioRecordatorio.java
├── application/
│   ├── port/
│   │   ├── CitaRepositoryPort.java
│   │   └── WhatsAppGatewayPort.java
│   └── usecase/
│       ├── EnviarRecordatorioUseCase.java
│       └── CancelarCitaUseCase.java
├── infrastructure/
│   ├── persistence/
│   │   ├── CitaJpaEntity.java
│   │   └── CitaJpaRepositoryAdapter.java
│   ├── whatsapp/
│   │   └── WhatsAppCloudApiAdapter.java
│   └── scheduling/
│       └── RecordatorioSchedulerJob.java
└── interfaces/
    └── web/
        └── WhatsAppWebhookController.java   # implementa la interfaz generada por openapi-generator

src/main/resources/openapi/
└── whatsapp-webhook.yaml   # copia versionada del contrato usada por el build (openapi-generator)

src/test/java/com/citassalud/
├── unit/                  # pruebas unitarias de domain/ y application/ (BDD Given/When/Then)
├── integration/           # pruebas de integración de infrastructure/ (JPA con H2, adaptador HTTP mockeado)
├── functional/            # pruebas de aceptación Cucumber (Gherkin) sobre los casos de uso end-to-end
└── architecture/          # pruebas ArchUnit de los límites de capas
```

**Decisión de Estructura**: Proyecto único (Opción 1: single project), ya que se trata de un backend Spring Boot sin frontend en este repositorio. La estructura de paquetes hace explícitas las cuatro capas de Arquitectura Limpia exigidas por el Principio I de la constitución (`domain`, `application`, `infrastructure`, `interfaces`), y los tres niveles de prueba exigidos por el Principio II (`unit`, `integration`, `functional`) se organizan como subcarpetas paralelas a esas capas.

## Seguimiento de Complejidad

*No aplica: el Constitution Check no registró violaciones.*
