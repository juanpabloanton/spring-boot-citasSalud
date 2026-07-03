<!--
Reporte de Impacto de Sincronización
====================================
Cambio de versión: PLANTILLA (sin versionar) → 1.0.0
Motivo del incremento: Ratificación inicial de la constitución del proyecto —
los cinco principios fundamentales se definen por primera vez (incremento
MAYOR según la política de versionado semántico para una adopción inicial).

Principios modificados:
  - I. [PRINCIPLE_1_NAME] → I. Arquitectura Limpia (Robert C. Martin) [NUEVO]
  - II. [PRINCIPLE_2_NAME] → II. Pruebas BDD: Unitarias, de Integración y Funcionales [NUEVO]
  - III. [PRINCIPLE_3_NAME] → III. SOLID, YAGNI y DRY [NUEVO]
  - IV. [PRINCIPLE_4_NAME] → IV. API First con Contratos OpenAPI [NUEVO]
  - V. [PRINCIPLE_5_NAME] → V. Puertas de Calidad Verificadas (Cobertura JaCoCo) [NUEVO]

Secciones añadidas:
  - Restricciones de Tecnología y Estructura del Proyecto (SECTION_2)
  - Flujo de Trabajo y Puertas de Calidad (SECTION_3)
  - Gobernanza (reglas completadas)

Secciones eliminadas: ninguna (los placeholders de la plantilla fueron
reemplazados por contenido concreto).

Plantillas que requieren actualización:
  ✅ .specify/templates/plan-template.md — la puerta "Constitution Check" es
     genérica y referencia este archivo dinámicamente; no requiere cambios.
  ✅ .specify/templates/tasks-template.md — actualizado: las tareas de
     pruebas dejaron de ser "OPCIONALES" y ahora son OBLIGATORIAS
     (unitarias/integración/funcionales BDD) según el Principio II, se
     añadió una tarea de contrato OpenAPI previa a la implementación y una
     tarea de verificación de cobertura JaCoCo en la fase de pulido.
  ✅ .specify/templates/spec-template.md — revisado, ya es agnóstico de
     framework y exige historias de usuario probables de forma
     independiente; no requiere cambios.
  ✅ .specify/templates/checklist-template.md — revisado, genérico; no
     requiere cambios.
  ✅ Archivos de comandos en .specify/templates/commands/ (si existen) —
     revisados, no se encontraron referencias obsoletas específicas de
     agente.

Seguimientos pendientes:
  - No existía una RATIFICATION_DATE documentada previamente para este
    proyecto; se estableció como la fecha de esta ratificación inicial
    (2026-07-02) al no existir una fecha de adopción formal anterior.
-->

# Constitución de CitasSalud

## Principios Fundamentales

### I. Arquitectura Limpia (Robert C. Martin)

El código base DEBE organizarse conforme a la Arquitectura Limpia (Clean
Architecture) de Robert C. Martin. La Regla de Dependencia es INNEGOCIABLE:
las dependencias del código fuente solo pueden apuntar hacia adentro, hacia
el dominio. En concreto:

- **Capa de Entidades/Dominio**: objetos y reglas de negocio centrales. NO
  DEBE tener ninguna dependencia de Spring, JPA, HTTP ni de ningún otro
  framework o preocupación de infraestructura.
- **Capa de Aplicación/Casos de Uso**: orquesta los objetos de dominio para
  cumplir un caso de uso específico. DEBE depender únicamente de la capa de
  dominio y de abstracciones (interfaces/puertos) que ella misma define para
  cualquier cosa que necesite del exterior.
- **Capa de Adaptadores de Interfaz**: controladores, presentadores,
  mapeadores de DTO, implementaciones de repositorios. DEBEN implementar los
  puertos definidos por la capa de aplicación y traducir entre el mundo
  exterior y los casos de uso.
- **Capa de Frameworks y Drivers**: Spring Boot, la base de datos, mensajería,
  clientes externos. Estos son plugins del núcleo de la aplicación y DEBEN
  poder reemplazarse sin tocar el código de dominio ni de aplicación.

La estructura de paquetes DEBE hacer explícitas estas capas (por ejemplo,
`domain`, `application`/`usecase`, `infrastructure`, `interfaces`/`web`).
Cualquier importación que apunte de afuera hacia adentro (por ejemplo, una
clase de dominio que importe un tipo de Spring o JPA) constituye una
violación de la constitución y DEBE ser rechazada en la revisión de código.
Cuando sea factible, los límites de la arquitectura DEBEN reforzarse con
pruebas de arquitectura automatizadas (por ejemplo, ArchUnit) además de la
revisión manual.

**Justificación**: la Arquitectura Limpia mantiene las reglas de negocio
independientes de los mecanismos de entrega y de las decisiones de
infraestructura, haciendo el sistema testeable, mantenible y resiliente a
los cambios de framework.

### II. Pruebas Primero con BDD: Unitarias, de Integración y Funcionales

Toda funcionalidad DEBE estar cubierta por tres niveles de pruebas
automatizadas, expresadas en estilo BDD (Given/When/Then, es decir,
Dado/Cuando/Entonces):

- **Pruebas unitarias**: ejercitan la lógica de dominio y de casos de uso de
  forma aislada, con todas las dependencias de capas externas simuladas
  (mocks/fakes).
- **Pruebas de integración**: verifican que los adaptadores (repositorios,
  mensajería, clientes externos) colaboran correctamente con infraestructura
  real o de pruebas (por ejemplo, una base de datos de pruebas).
- **Pruebas funcionales/de aceptación**: validan escenarios de negocio
  completos de extremo a extremo desde la perspectiva de la API/usuario,
  escritos como escenarios Dado/Cuando/Entonces (por ejemplo, mediante
  Cucumber/Gherkin o un framework BDD equivalente).

Las pruebas DEBEN escribirse antes o junto con la implementación
correspondiente y DEBEN fallar antes de que dicha implementación exista,
siempre que sea prácticamente viable (test-first, ciclo rojo-verde-refactor).
Las pruebas son la especificación ejecutable del comportamiento del sistema,
no un añadido de último momento. Un pull request que introduzca nuevo
comportamiento sin la correspondiente cobertura BDD unitaria, de integración
y funcional NO DEBE fusionarse.

**Justificación**: los escenarios BDD mantienen las pruebas legibles como
especificaciones vivas y obligan a definir el comportamiento en términos de
negocio antes de escribir el código, mientras que los tres niveles de
prueba detectan defectos en la capa más económica posible.

### III. SOLID, YAGNI y DRY

Todo el código de producción DEBE seguir SOLID:

- **Responsabilidad Única**: cada clase/módulo tiene exactamente una razón
  para cambiar.
- **Abierto/Cerrado**: el comportamiento se extiende mediante código nuevo
  (nuevas implementaciones de un puerto/interfaz), no modificando código
  estable y ya probado.
- **Sustitución de Liskov**: los subtipos/implementaciones DEBEN poder
  sustituir a sus abstracciones sin romper a quienes las consumen.
- **Segregación de Interfaces**: los consumidores NO DEBEN verse obligados a
  depender de métodos que no usan; se prefieren interfaces/puertos pequeños
  y enfocados.
- **Inversión de Dependencias**: los módulos de alto nivel (casos de uso)
  DEBEN depender de abstracciones, y esas abstracciones DEBEN ser propiedad
  del módulo de alto nivel, no de la infraestructura que las implementa —
  esta es la misma regla que sustenta la Regla de Dependencia de la
  Arquitectura Limpia (Principio I).

**YAGNI**: no se pueden añadir abstracciones especulativas, parámetros de
configuración ni puntos de extensión para requisitos futuros hipotéticos.
Se construye únicamente lo que exige la especificación/tarea actual.

**DRY**: la lógica duplicada DEBE consolidarse en una única fuente de
verdad. DRY NO DEBE usarse para justificar la fusión de lógica que solo es
superficialmente similar pero conceptualmente distinta — dichas fusiones
también violan la Responsabilidad Única y constituyen igualmente una
violación de la constitución.

La revisión de código DEBE verificar explícitamente y rechazar violaciones
de SOLID, YAGNI y DRY.

**Justificación**: estas prácticas son la disciplina diaria que mantiene
saludables, con el tiempo, los límites de la Arquitectura Limpia definidos
en el Principio I, y evitan tanto la complejidad prematura como la
duplicación innecesaria.

### IV. API First con Contratos OpenAPI

Toda API DEBE diseñarse contract-first (contrato primero):

- Se DEBE redactar y revisar/aprobar una especificación OpenAPI (3.x) ANTES
  de implementar cualquier controlador, cliente o lógica de negocio ligada
  a esa API.
- El contrato OpenAPI es la única fuente de verdad para las formas de
  solicitud/respuesta, códigos de estado, cabeceras y modelos de error.
- Las interfaces/DTOs del lado del servidor (y del cliente, cuando
  corresponda) DEBEN generarse a partir del contrato OpenAPI aprobado
  usando `openapi-generator`, integrado en el build (Gradle), en lugar de
  escribirse a mano desde cero. El código escrito manualmente se limita a
  la lógica de negocio que implementa las interfaces generadas.
- Cualquier cambio en el comportamiento de la API DEBE comenzar actualizando
  el contrato OpenAPI, regenerando el código y solo después ajustando la
  lógica de negocio. El contrato y la implementación NUNCA DEBEN divergir —
  un PR que cambie el comportamiento en tiempo de ejecución de la API sin el
  correspondiente cambio de contrato NO DEBE fusionarse.

**Justificación**: el desarrollo contract-first ofrece a los consumidores
una interfaz estable y revisable, permite el trabajo paralelo de
cliente/servidor y elimina toda una clase de errores causados por la
divergencia entre implementación y documentación.

### V. Puertas de Calidad Verificadas (Cobertura JaCoCo)

La cobertura de pruebas automatizada DEBE medirse con JaCoCo en cada build:

- **La cobertura de líneas por clase DEBE superar el 80% (> 80%)** para
  cada clase de producción.
- **La cobertura de líneas global/general del proyecto DEBE ser al menos
  del 80% (>= 80%)**.
- El build DEBE fallar (mediante una regla de verificación de cobertura de
  JaCoCo vinculada a la tarea `check`/CI) si alguno de los umbrales no se
  cumple.
- Se DEBE generar un reporte de cobertura de JaCoCo (HTML y/o XML) en cada
  ejecución de CI y DEBE estar disponible para su revisión.
- La cobertura DEBE obtenerse mediante las pruebas BDD unitarias, de
  integración y funcionales significativas exigidas por el Principio II.
  Las pruebas triviales o tautológicas escritas únicamente para inflar el
  porcentaje de cobertura constituyen, en sí mismas, una violación de la
  constitución.

**Justificación**: un umbral de cobertura obligatorio y verificado
automáticamente convierte la disciplina de pruebas del Principio II en una
puerta objetiva, comprobada por CI, en lugar de depender de la diligencia
individual.

## Restricciones de Tecnología y Estructura del Proyecto

El stack de implementación es Spring Boot sobre la JVM, construido con
Gradle. La organización de paquetes DEBE hacer explícitas las capas de
Arquitectura Limpia del Principio I en el nivel superior (por ejemplo,
`domain`, `application`, `infrastructure`, `interfaces`). Los contratos
OpenAPI DEBEN residir en una ubicación dedicada y versionada (por ejemplo,
`src/main/resources/openapi/` o un directorio `api/`) y DEBEN integrarse en
el build de Gradle mediante el plugin de Gradle `openapi-generator`, de modo
que el código generado se produzca como parte del build normal. El plugin
de Gradle de JaCoCo DEBE aplicarse, con su tarea de verificación de
cobertura vinculada a la tarea del ciclo de vida `check`, de forma que los
umbrales del Principio V se apliquen automáticamente en lugar de
manualmente.

## Flujo de Trabajo y Puertas de Calidad

Un pull request NO DEBE fusionarse si se cumple alguna de las siguientes
condiciones:

- No se alcanzan los umbrales de cobertura de JaCoCo por clase (> 80%) o
  global (>= 80%).
- Falta un cambio en el contrato OpenAPI, o el código generado no fue
  regenerado a partir del contrato vigente.
- Se viola un límite de capa de la Arquitectura Limpia (verificado mediante
  revisión de código y, cuando existan, pruebas de arquitectura).
- Faltan las pruebas BDD unitarias, de integración o funcionales requeridas
  para el comportamiento nuevo o modificado.
- Se identifica en la revisión una violación de SOLID, YAGNI o DRY que no
  se resuelve.

La revisión de código es el principal punto de control para el cumplimiento
arquitectónico y de buenas prácticas; las verificaciones automatizadas de
JaCoCo y de generación de build son el respaldo automático.

## Gobernanza

Esta constitución prevalece sobre cualquier convención de ingeniería,
tutorial o práctica previa en conflicto dentro de este repositorio. Todo
plan de funcionalidad DEBE pasar la puerta "Constitution Check" en
`plan.md` antes de iniciar la Fase 0 de investigación, y DEBE volver a
verificarse después del diseño de la Fase 1; cualquier desviación de un
principio de este documento DEBE justificarse explícitamente en la sección
de Seguimiento de Complejidad del plan, o la desviación DEBE eliminarse.

**Procedimiento de enmienda**: las enmiendas se proponen mediante un cambio
documentado (qué cambia y por qué), son revisadas y aprobadas por el/los
mantenedor(es) del proyecto, y luego se aplican a este archivo junto con un
incremento de versión.

**Política de versionado** (versionado semántico aplicado a la gobernanza):
- **MAYOR (MAJOR)**: eliminación o redefinición incompatible hacia atrás de
  un principio o regla de gobernanza.
- **MENOR (MINOR)**: se añade un nuevo principio o sección, o se amplía de
  forma material una guía existente.
- **PARCHE (PATCH)**: aclaraciones, redacción, corrección de errores
  tipográficos y otros refinamientos no semánticos.

**Revisión de cumplimiento**: todo pull request DEBE verificarse contra la
sección de Flujo de Trabajo y Puertas de Calidad anterior antes de
fusionarse. Usar `CLAUDE.md` para guía adicional de desarrollo en tiempo de
ejecución/agente cuando esta constitución no descienda al nivel de detalle
de implementación; `CLAUDE.md` NO DEBE contradecir esta constitución.

**Versión**: 1.0.0 | **Ratificada**: 2026-07-02 | **Última Enmienda**: 2026-07-02
