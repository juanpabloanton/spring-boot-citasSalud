---

description: "Lista de tareas para la funcionalidad: Recordatorio de Cita por WhatsApp"
---

# Tareas: Recordatorio de Cita por WhatsApp

**Entrada**: Documentos de diseño desde `specs/001-recordatorio-whatsapp-citas/`
**Prerrequisitos**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/whatsapp-webhook.openapi.yaml`, `quickstart.md`

**Pruebas**: Según la constitución del proyecto (Arquitectura Limpia + BDD Test-First), las pruebas unitarias, de integración y funcionales BDD son OBLIGATORIAS para ambas historias de usuario — no son opcionales. La Historia 2 expone una API (webhook), por lo que incluye además una prueba de contrato basada en el OpenAPI ya definido. La fase de Pulido incluye la verificación de cobertura JaCoCo.

**Organización**: Las tareas se agrupan por historia de usuario para permitir implementación y prueba independientes de cada una.

## Formato: `[ID] [P?] [Historia] Descripción`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias)
- **[Historia]**: A qué historia de usuario pertenece la tarea (US1, US2)
- Cada tarea incluye la ruta de archivo exacta

## Convenciones de Ruta

Proyecto único (Spring Boot, sin frontend), según `plan.md`:

- Código de producción: `src/main/java/com/citassalud/{domain,application,infrastructure,interfaces}/`
- Contrato OpenAPI versionado: `src/main/resources/openapi/whatsapp-webhook.yaml`
- Pruebas: `src/test/java/com/citassalud/{unit,integration,functional,contract,architecture}/`
- Escenarios Gherkin: `src/test/resources/features/`

---

## Fase 1: Setup (Infraestructura Compartida)

**Propósito**: Preparar el build para soportar Arquitectura Limpia, OpenAPI First y cobertura JaCoCo antes de tocar código de negocio.

- [X] T001 Añadir en `build.gradle` las dependencias: `spring-boot-starter-data-jpa`, cliente HTTP para WhatsApp Cloud API, Cucumber-JVM (`io.cucumber:cucumber-java`, `io.cucumber:cucumber-junit-platform-engine`), ArchUnit (`com.tngtech.archunit:archunit-junit5`), y WireMock para pruebas de integración del adaptador HTTP
- [X] T002 [P] Aplicar y configurar el plugin Gradle `org.openapi.generator` en `build.gradle`, apuntando a `src/main/resources/openapi/whatsapp-webhook.yaml` con `interfaceOnly=true` (según decisión de `research.md` §6)
- [X] T003 [P] Aplicar y configurar el plugin Gradle `jacoco` en `build.gradle` con `jacocoTestCoverageVerification` ligada a `check`: regla `CLASS` mínimo 0.80 y regla `BUNDLE` mínimo 0.80, más `jacocoTestReport` (HTML + XML) (según `research.md` §7)
- [X] T004 [P] Copiar `specs/001-recordatorio-whatsapp-citas/contracts/whatsapp-webhook.openapi.yaml` a `src/main/resources/openapi/whatsapp-webhook.yaml` como fuente de verdad usada por el build
- [X] T005 [P] Configurar el motor de Cucumber sobre JUnit 5 Platform (`@Suite` con `@IncludeEngines("cucumber")`, `@SelectClasspathResource("features")`) en `src/test/java/com/citassalud/functional/CucumberSuite.java`

**Checkpoint**: El build compila con las dependencias y plugins nuevos; `./gradlew check` corre (aunque sin tests todavía) sin errores de configuración.

---

## Fase 2: Fundacional (Prerrequisitos Bloqueantes)

**Propósito**: Núcleo de dominio, puertos y verificación de límites de arquitectura que ambas historias de usuario necesitan.

**⚠️ CRÍTICO**: Ninguna historia de usuario puede comenzar hasta completar esta fase.

- [X] T006 [P] Crear `EstadoCita` (enum: `AGENDADA`, `RECORDATORIO_ENVIADO`, `CANCELADA`, `COMPLETADA`) en `src/main/java/com/citassalud/domain/cita/EstadoCita.java`
- [X] T007 [US-comparte] Crear entidad de dominio `Cita` (id, pacienteId, medicoId, fechaHora, estado, recordatorioEnviadoEn) en `src/main/java/com/citassalud/domain/cita/Cita.java` (depende de T006)
- [X] T008 [P] Crear entidad de dominio `Paciente` (id, nombre, numeroWhatsapp) en `src/main/java/com/citassalud/domain/paciente/Paciente.java`
- [X] T009 [P] Crear entidad de dominio `Medico` (id, nombre) en `src/main/java/com/citassalud/domain/medico/Medico.java`
- [X] T010 [P] Crear `EstadoEnvioRecordatorio` (enum: `PENDIENTE`, `ENVIADO`, `FALLIDO`, `SIN_NUMERO_VALIDO`, `RESPONDIDO_CANCELACION`, `RESPONDIDO_NO_RECONOCIDO`) en `src/main/java/com/citassalud/domain/recordatorio/EstadoEnvioRecordatorio.java`
- [X] T011 Crear entidad de dominio `Recordatorio` (id, citaId, canal, estadoEnvio, enviadoEn, intentos, mensajeProveedorId) en `src/main/java/com/citassalud/domain/recordatorio/Recordatorio.java` (depende de T010)
- [X] T012 Implementar `VentanaCancelacionPolicy` (regla de dominio única y reutilizable: cancelación válida solo si `fechaHora - ahora >= 2h`, FR-008) en `src/main/java/com/citassalud/domain/cita/VentanaCancelacionPolicy.java` (depende de T007)
- [X] T013 [P] Definir el puerto `CitaRepositoryPort` (buscar por id, buscar citas agendadas en ventana de 24h, guardar) en `src/main/java/com/citassalud/application/port/CitaRepositoryPort.java`
- [X] T014 [P] Definir el puerto `WhatsAppGatewayPort` (enviarRecordatorio, enviarConfirmacionCancelacion, enviarAclaracion) en `src/main/java/com/citassalud/application/port/WhatsAppGatewayPort.java`
- [X] T015 Configurar la regla base de ArchUnit ("ninguna clase de `domain` depende de `org.springframework..`, `jakarta.persistence..`, `infrastructure..` ni `interfaces..`") en `src/test/java/com/citassalud/architecture/ArquitecturaLimpiaTest.java` (según `research.md` §5)

**Checkpoint**: Dominio y puertos existen y compilan; ArchUnit pasa sobre el dominio vacío. Las historias de usuario pueden comenzar en paralelo.

---

## Fase 3: Historia de Usuario 1 - Recordatorio automático de cita por WhatsApp (Prioridad: P1) 🎯 MVP

**Objetivo**: Enviar automáticamente un recordatorio de WhatsApp con fecha, hora y nombre de la médica/médico 24 horas antes de cada cita agendada.

**Prueba Independiente**: Agendar una cita para dentro de 24 horas, ejecutar el job de recordatorio y verificar (vía el adaptador de WhatsApp mockeado) que se envió un mensaje con la fecha, hora y nombre de la médica/médico, y que la `Cita` pasó a `RECORDATORIO_ENVIADO`.

### Pruebas para la Historia de Usuario 1 (OBLIGATORIO — BDD unitarias, integración y funcionales) ⚠️

> **NOTA: Escribir estas pruebas PRIMERO (Dado/Cuando/Entonces), asegurarse de que FALLEN antes de implementar**

- [X] T016 [P] [US1] Escenario Gherkin de HU1 (3 escenarios de aceptación de `spec.md`: envío exitoso, sin número válido, múltiples citas) en `src/test/resources/features/recordatorio.feature`
- [X] T017 [P] [US1] Step definitions del escenario de HU1 en `src/test/java/com/citassalud/functional/RecordatorioStepDefinitions.java`
- [X] T018 [P] [US1] Prueba unitaria (Dado/Cuando/Entonces) de `EnviarRecordatorioUseCase` en `src/test/java/com/citassalud/unit/EnviarRecordatorioUseCaseTest.java`
- [X] T019 [P] [US1] Prueba de integración de `CitaJpaRepositoryAdapter` contra H2 embebida (persistencia y consulta por ventana de 24h) en `src/test/java/com/citassalud/integration/CitaJpaRepositoryAdapterIT.java`
- [X] T020 [P] [US1] Prueba de integración de `WhatsAppCloudApiAdapter` con WireMock (éxito, fallo temporal con reintento, número inválido) en `src/test/java/com/citassalud/integration/WhatsAppCloudApiAdapterIT.java`

### Implementación de la Historia de Usuario 1

- [X] T021 [US1] Implementar `EnviarRecordatorioUseCase` (orquesta `CitaRepositoryPort` + `WhatsAppGatewayPort`, marca `RECORDATORIO_ENVIADO`, maneja el caso sin número válido con registro y notificación administrativa según FR-001/FR-006) en `src/main/java/com/citassalud/application/usecase/EnviarRecordatorioUseCase.java` (depende de T007, T011, T013, T014)
- [X] T022 [US1] Implementar `CitaJpaEntity` y su mapeo hacia/desde `Cita` en `src/main/java/com/citassalud/infrastructure/persistence/CitaJpaEntity.java`
- [X] T023 [US1] Implementar `CitaJpaRepositoryAdapter` (implementa `CitaRepositoryPort` sobre Spring Data JPA) en `src/main/java/com/citassalud/infrastructure/persistence/CitaJpaRepositoryAdapter.java` (depende de T022)
- [X] T024 [US1] Implementar `WhatsAppCloudApiAdapter` (implementa `WhatsAppGatewayPort`, integra WhatsApp Cloud API de Meta, reintento hasta 3 veces con espaciamiento creciente según Suposiciones de `spec.md`) en `src/main/java/com/citassalud/infrastructure/whatsapp/WhatsAppCloudApiAdapter.java`
- [X] T025 [US1] Implementar `RecordatorioSchedulerJob` (`@Scheduled(fixedRate=...)`, consulta citas en `[ahora+24h, ahora+24h+margen]` sin recordatorio y dispara `EnviarRecordatorioUseCase`, según `research.md` §2) en `src/main/java/com/citassalud/infrastructure/scheduling/RecordatorioSchedulerJob.java` (depende de T021)
- [X] T026 [US1] Añadir el registro de auditoría de cada intento de envío (enviado, fallido, sin número válido — FR-006) en `EnviarRecordatorioUseCase`

**Checkpoint**: En este punto, la Historia de Usuario 1 debe funcionar de forma completa e independiente — es el MVP desplegable.

---

## Fase 4: Historia de Usuario 2 - Cancelación de la cita respondiendo al recordatorio (Prioridad: P2)

**Objetivo**: Permitir que el paciente cancele su cita respondiendo "CANCELAR" al recordatorio de WhatsApp, liberando la franja horaria automáticamente.

**Prueba Independiente**: Con un recordatorio ya enviado (estado `RECORDATORIO_ENVIADO`), enviar al webhook un payload de respuesta con `text.body = "CANCELAR"` y verificar que la `Cita` pasa a `CANCELADA` y se envía la confirmación por WhatsApp.

### Pruebas para la Historia de Usuario 2 (OBLIGATORIO — BDD unitarias, integración y funcionales) ⚠️

> **NOTA: Escribir estas pruebas PRIMERO (Dado/Cuando/Entonces), asegurarse de que FALLEN antes de implementar**

- [X] T027 [P] [US2] Escenario Gherkin de HU2 (3 escenarios de aceptación de `spec.md`: cancelación exitosa, respuesta no reconocida, confirmación de cancelación) en `src/test/resources/features/cancelacion.feature`
- [X] T028 [P] [US2] Step definitions del escenario de HU2 en `src/test/java/com/citassalud/functional/CancelacionStepDefinitions.java`
- [X] T029 [P] [US2] Prueba unitaria (Dado/Cuando/Entonces) de `CancelarCitaUseCase`, incluyendo la ventana de 2h (FR-008), la palabra clave exacta (FR-003) y la resolución por hilo del recordatorio con múltiples citas activas (FR-009) en `src/test/java/com/citassalud/unit/CancelarCitaUseCaseTest.java`
- [X] T030 [P] [US2] Prueba de contrato del endpoint `POST /api/v1/whatsapp/webhook` a partir de `contracts/whatsapp-webhook.openapi.yaml` en `src/test/java/com/citassalud/contract/WhatsAppWebhookContractTest.java`
- [X] T031 [P] [US2] Prueba de integración de `WhatsAppWebhookController` con `MockMvc` (incluye verificación GET del webhook) en `src/test/java/com/citassalud/integration/WhatsAppWebhookControllerIT.java`

### Implementación de la Historia de Usuario 2

- [X] T032 [US2] Generar la interfaz del controlador del webhook con `openapi-generator` a partir de `src/main/resources/openapi/whatsapp-webhook.yaml` (verificar salida de `./gradlew openApiGenerate`, sin escribir código a mano) — depende de T002, T004
- [X] T033 [US2] Implementar `CancelarCitaUseCase` (valida `VentanaCancelacionPolicy`, resuelve la `Cita` por `Recordatorio.citaId` del hilo, aplica FR-003a para respuestas no reconocidas) en `src/main/java/com/citassalud/application/usecase/CancelarCitaUseCase.java` (depende de T011, T012, T013, T014)
- [X] T034 [US2] Implementar `WhatsAppWebhookController` (implementa la interfaz generada por `openapi-generator`, delega en `CancelarCitaUseCase`) en `src/main/java/com/citassalud/interfaces/web/WhatsAppWebhookController.java` (depende de T032, T033)
- [X] T035 [US2] Implementar el envío de confirmación de cancelación exitosa por WhatsApp (FR-005) en `CancelarCitaUseCase` / `WhatsAppCloudApiAdapter`
- [X] T036 [US2] Implementar el reenvío de aclaración de opciones válidas cuando la respuesta no es reconocida (FR-003a) en `CancelarCitaUseCase`

**Checkpoint**: En este punto, ambas historias de usuario deben funcionar de forma independiente y en conjunto.

---

## Fase Final: Pulido y Aspectos Transversales

**Propósito**: Verificación de calidad exigida por la constitución antes de considerar la funcionalidad terminada.

- [X] T037 [P] Actualizar `README.md`/documentación del proyecto con el nuevo flujo de recordatorio y cancelación por WhatsApp
- [X] T038 Ejecutar `./gradlew check` y verificar el reporte de cobertura JaCoCo en `build/reports/jacoco/test/html/index.html` (>80% por clase, >=80% global); el build DEBE fallar si no se cumplen los umbrales
- [X] T039 Verificar que la prueba de arquitectura `ArquitecturaLimpiaTest` (T015) sigue en verde tras la implementación completa de ambas historias
- [X] T040 Ejecutar manualmente el flujo descrito en `quickstart.md` (agendar cita, disparar el job, responder "CANCELAR" al webhook) y confirmar que la franja queda disponible en menos de 5 minutos (SC-004)

---

## Dependencias y Orden de Ejecución

### Dependencias de Fase

- **Setup (Fase 1)**: Sin dependencias — puede iniciar de inmediato
- **Fundacional (Fase 2)**: Depende de Setup — BLOQUEA ambas historias de usuario
- **Historia de Usuario 1 (Fase 3)**: Depende de Fundacional; sin dependencia de Historia 2
- **Historia de Usuario 2 (Fase 4)**: Depende de Fundacional; usa el mismo `Recordatorio`/`Cita` que produce la Historia 1, por lo que en la práctica se implementa después de la Historia 1 (aunque las tareas de puertos/dominio son compartidas desde la Fase 2)
- **Pulido (Fase Final)**: Depende de que ambas historias estén completas

### Dentro de Cada Historia de Usuario

- Las pruebas (T016–T020 para US1, T027–T031 para US2) DEBEN escribirse y FALLAR antes de la implementación correspondiente
- Entidades/dominio antes que casos de uso; casos de uso antes que adaptadores de infraestructura; adaptadores antes que el controlador/scheduler que los usa

### Oportunidades de Paralelismo

- Todas las tareas de Setup marcadas [P] pueden ejecutarse en paralelo (T002–T005)
- Dentro de la Fase Fundacional, T006, T008, T009, T010, T013, T014 pueden ejecutarse en paralelo; T007, T011, T012, T015 tienen dependencias directas señaladas
- Dentro de cada historia, todas las pruebas marcadas [P] pueden escribirse en paralelo antes de empezar la implementación
- Una vez completa la Fase Fundacional, un segundo desarrollador podría adelantar las pruebas de la Historia 2 (T027–T031) en paralelo con la implementación de la Historia 1, aunque la implementación de la Historia 2 depende de que `Recordatorio`/`Cita` de la Historia 1 estén operativos en la práctica

---

## Ejemplo de Ejecución en Paralelo: Historia de Usuario 1

```bash
# Lanzar juntas todas las pruebas de la Historia de Usuario 1:
Task: "Escenario Gherkin de HU1 en src/test/resources/features/recordatorio.feature"
Task: "Prueba unitaria de EnviarRecordatorioUseCase en src/test/java/com/citassalud/unit/EnviarRecordatorioUseCaseTest.java"
Task: "Prueba de integración de CitaJpaRepositoryAdapter en src/test/java/com/citassalud/integration/CitaJpaRepositoryAdapterIT.java"
Task: "Prueba de integración de WhatsAppCloudApiAdapter en src/test/java/com/citassalud/integration/WhatsAppCloudApiAdapterIT.java"
```

---

## Estrategia de Implementación

### MVP Primero (Solo Historia de Usuario 1)

1. Completar Fase 1: Setup
2. Completar Fase 2: Fundacional (CRÍTICO — bloquea ambas historias)
3. Completar Fase 3: Historia de Usuario 1
4. **DETENER Y VALIDAR**: probar la Historia de Usuario 1 de forma independiente (agendar cita, verificar envío del recordatorio)
5. Desplegar/demostrar si está listo — ya reduce inasistencias aunque todavía no permita auto-cancelación

### Entrega Incremental

1. Completar Setup + Fundacional → base lista
2. Añadir Historia de Usuario 1 → probar de forma independiente → Desplegar/Demo (¡MVP!)
3. Añadir Historia de Usuario 2 → probar de forma independiente → Desplegar/Demo
4. Cada historia aporta valor sin romper la anterior

---

## Notas

- [P] = archivos distintos, sin dependencias entre sí
- La etiqueta [Historia] mapea la tarea a la historia de usuario correspondiente para trazabilidad
- Cada historia de usuario debe ser completable y probable de forma independiente
- Verificar que las pruebas fallan antes de implementar (rojo-verde-refactor, Principio II de la constitución)
- Hacer commit después de cada tarea o grupo lógico
- Detenerse en cada checkpoint para validar la historia de forma independiente
- Evitar: tareas vagas, conflictos de archivo entre tareas paralelas, dependencias cruzadas entre historias que rompan la independencia
