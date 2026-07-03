---

description: "Task list template for feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Pruebas**: Según la constitución del proyecto (Arquitectura Limpia + BDD Test-First), las pruebas unitarias, de integración y funcionales BDD son OBLIGATORIAS para toda historia de usuario que introduzca o modifique comportamiento — no son opcionales. Toda historia que exponga una API DEBE incluir además una tarea de contrato OpenAPI antes de la implementación, y la fase de Pulido DEBE incluir una tarea de verificación de cobertura JaCoCo (>80% por clase, >=80% global).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Single project**: `src/`, `tests/` at repository root
- **Web app**: `backend/src/`, `frontend/src/`
- **Mobile**: `api/src/`, `ios/src/` or `android/src/`
- Paths shown below assume single project - adjust based on plan.md structure

<!--
  ============================================================================
  IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only.

  The /speckit-tasks command MUST replace these with actual tasks based on:
  - User stories from spec.md (with their priorities P1, P2, P3...)
  - Feature requirements from plan.md
  - Entities from data-model.md
  - Endpoints from contracts/

  Tasks MUST be organized by user story so each story can be:
  - Implemented independently
  - Tested independently
  - Delivered as an MVP increment

  DO NOT keep these sample tasks in the generated tasks.md file.
  ============================================================================
-->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create project structure per implementation plan
- [ ] T002 Initialize [language] project with [framework] dependencies
- [ ] T003 [P] Configure linting and formatting tools

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

Examples of foundational tasks (adjust based on your project):

- [ ] T004 Setup database schema and migrations framework
- [ ] T005 [P] Implement authentication/authorization framework
- [ ] T006 [P] Setup API routing and middleware structure
- [ ] T007 Create base models/entities that all stories depend on
- [ ] T008 Configure error handling and logging infrastructure
- [ ] T009 Setup environment configuration management

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - [Title] (Priority: P1) 🎯 MVP

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Pruebas para la Historia de Usuario 1 (OBLIGATORIO — BDD unitarias, integración y funcionales) ⚠️

> **NOTA: Escribir estas pruebas PRIMERO (Dado/Cuando/Entonces), asegurarse de que FALLEN antes de implementar**

- [ ] T010 [P] [US1] Definir/actualizar el contrato OpenAPI para [endpoint] en src/main/resources/openapi/[name].yaml
- [ ] T011 [P] [US1] Prueba de contrato para [endpoint] en tests/contract/test_[name].py
- [ ] T012 [P] [US1] Prueba unitaria (Dado/Cuando/Entonces) para [dominio/caso de uso] en tests/unit/test_[name].py
- [ ] T013 [P] [US1] Prueba de integración (Dado/Cuando/Entonces) para [adaptador/repositorio] en tests/integration/test_[name].py
- [ ] T014 [P] [US1] Prueba funcional/de aceptación (Dado/Cuando/Entonces) para [flujo de usuario] en tests/functional/test_[name].py

### Implementación de la Historia de Usuario 1

- [ ] T015 [P] [US1] Crear modelo de dominio [Entidad1] en src/domain/[entity1].py
- [ ] T016 [P] [US1] Crear modelo de dominio [Entidad2] en src/domain/[entity2].py
- [ ] T017 [US1] Implementar [CasoDeUso] en src/application/[usecase].py (depende de T015, T016)
- [ ] T018 [US1] Implementar el adaptador de [endpoint/funcionalidad] (interfaz generada por OpenAPI) en src/interfaces/[file].py
- [ ] T019 [US1] Añadir validación y manejo de errores
- [ ] T020 [US1] Añadir logging para las operaciones de la historia de usuario 1

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Pruebas para la Historia de Usuario 2 (OBLIGATORIO — BDD unitarias, integración y funcionales) ⚠️

> **NOTA: Escribir estas pruebas PRIMERO (Dado/Cuando/Entonces), asegurarse de que FALLEN antes de implementar**

- [ ] T021 [P] [US2] Definir/actualizar el contrato OpenAPI para [endpoint] en src/main/resources/openapi/[name].yaml
- [ ] T022 [P] [US2] Prueba de contrato para [endpoint] en tests/contract/test_[name].py
- [ ] T023 [P] [US2] Prueba unitaria (Dado/Cuando/Entonces) para [dominio/caso de uso] en tests/unit/test_[name].py
- [ ] T024 [P] [US2] Prueba de integración (Dado/Cuando/Entonces) para [adaptador/repositorio] en tests/integration/test_[name].py
- [ ] T025 [P] [US2] Prueba funcional/de aceptación (Dado/Cuando/Entonces) para [flujo de usuario] en tests/functional/test_[name].py

### Implementación de la Historia de Usuario 2

- [ ] T026 [P] [US2] Crear modelo de dominio [Entidad] en src/domain/[entity].py
- [ ] T027 [US2] Implementar [CasoDeUso] en src/application/[usecase].py
- [ ] T028 [US2] Implementar el adaptador de [endpoint/funcionalidad] (interfaz generada por OpenAPI) en src/interfaces/[file].py
- [ ] T029 [US2] Integrar con los componentes de la Historia de Usuario 1 (si es necesario)

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Pruebas para la Historia de Usuario 3 (OBLIGATORIO — BDD unitarias, integración y funcionales) ⚠️

> **NOTA: Escribir estas pruebas PRIMERO (Dado/Cuando/Entonces), asegurarse de que FALLEN antes de implementar**

- [ ] T030 [P] [US3] Definir/actualizar el contrato OpenAPI para [endpoint] en src/main/resources/openapi/[name].yaml
- [ ] T031 [P] [US3] Prueba de contrato para [endpoint] en tests/contract/test_[name].py
- [ ] T032 [P] [US3] Prueba unitaria (Dado/Cuando/Entonces) para [dominio/caso de uso] en tests/unit/test_[name].py
- [ ] T033 [P] [US3] Prueba de integración (Dado/Cuando/Entonces) para [adaptador/repositorio] en tests/integration/test_[name].py
- [ ] T034 [P] [US3] Prueba funcional/de aceptación (Dado/Cuando/Entonces) para [flujo de usuario] en tests/functional/test_[name].py

### Implementación de la Historia de Usuario 3

- [ ] T035 [P] [US3] Crear modelo de dominio [Entidad] en src/domain/[entity].py
- [ ] T036 [US3] Implementar [CasoDeUso] en src/application/[usecase].py
- [ ] T037 [US3] Implementar el adaptador de [endpoint/funcionalidad] (interfaz generada por OpenAPI) en src/interfaces/[file].py

**Checkpoint**: All user stories should now be independently functional

---

[Add more user story phases as needed, following the same pattern]

---

## Fase N: Pulido y Aspectos Transversales

**Propósito**: Mejoras que afectan a varias historias de usuario

- [ ] TXXX [P] Actualizaciones de documentación en docs/
- [ ] TXXX Limpieza de código y refactorización
- [ ] TXXX Optimización de rendimiento en todas las historias
- [ ] TXXX [P] Pruebas unitarias adicionales de cobertura en tests/unit/
- [ ] TXXX Endurecimiento de seguridad (security hardening)
- [ ] TXXX Ejecutar la validación de quickstart.md
- [ ] TXXX Ejecutar `./gradlew check` y verificar el reporte de cobertura JaCoCo (>80% por clase, >=80% global); el build DEBE fallar si no se cumplen los umbrales

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable

### Within Each User Story

- Tests (if included) MUST be written and FAIL before implementation
- Models before services
- Services before endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together (if tests requested):
Task: "Contract test for [endpoint] in tests/contract/test_[name].py"
Task: "Integration test for [user journey] in tests/integration/test_[name].py"

# Launch all models for User Story 1 together:
Task: "Create [Entity1] model in src/models/[entity1].py"
Task: "Create [Entity2] model in src/models/[entity2].py"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
