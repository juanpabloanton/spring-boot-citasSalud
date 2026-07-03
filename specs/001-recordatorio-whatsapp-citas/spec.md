# Especificación de Funcionalidad: Recordatorio de Cita por WhatsApp

**Rama de la funcionalidad**: `001-recordatorio-whatsapp-citas`

**Creada**: 2026-07-03

**Estado**: Borrador

**Entrada**: Descripción del usuario: "[US-03] Como paciente, quiero recibir un recordatorio automático por WhatsApp antes de mi cita para no olvidarla ni generar una inasistencia sin previo aviso. Criterios de aceptación: Dado que un paciente tiene una cita agendada, cuando faltan 24 horas para el turno, entonces el sistema envía un mensaje de WhatsApp con fecha, hora y nombre de la médico. Dado que el paciente recibe el recordatorio, cuando responde que cancela, entonces la cita queda anulada y la franja vuelve a estar disponible en la agenda."

## Clarifications

### Session 2026-07-03

- Q: ¿Cómo debe reconocerse la intención de cancelar en la respuesta del paciente al recordatorio? → A: Palabra clave exacta (ej. "CANCELAR"), sin interpretación de lenguaje libre ni botones interactivos.
- Q: ¿Hasta cuánto tiempo antes del turno se permite cancelar por WhatsApp? → A: Hasta 2 horas antes del turno; después de ese límite el canal deja de aceptar la cancelación y se le indica al paciente que contacte directamente al centro de salud.
- Q: Cuando el paciente tiene más de una cita activa, ¿a cuál cita se asocia la cancelación? → A: Siempre a la cita del hilo/recordatorio específico en el que el paciente responde; no se pide confirmación adicional.

## Escenarios de Usuario y Pruebas *(obligatorio)*

### Historia de Usuario 1 - Recordatorio automático de cita por WhatsApp (Prioridad: P1)

Como paciente, quiero recibir un recordatorio automático por WhatsApp antes de mi cita para no olvidarla ni generar una inasistencia sin previo aviso.

**Por qué esta prioridad**: Es el núcleo del valor de la funcionalidad: reduce las inasistencias notificando proactivamente al paciente. Sin este envío no existe ningún valor que ofrecer, por lo que es el mínimo producto viable.

**Prueba independiente**: Puede probarse completamente agendando una cita para dentro de 24 horas y verificando que el paciente recibe un mensaje de WhatsApp con la fecha, la hora y el nombre de la médico antes del turno.

**Escenarios de Aceptación**:

1. **Dado** que un paciente tiene una cita agendada, **Cuando** faltan 24 horas para el turno, **Entonces** el sistema envía un mensaje de WhatsApp con fecha, hora y nombre de la médico.
2. **Dado** que un paciente tiene una cita agendada pero no tiene un número de WhatsApp válido registrado, **Cuando** faltan 24 horas para el turno, **Entonces** el sistema registra el intento fallido y notifica al área administrativa en lugar de fallar silenciosamente.
3. **Dado** que un paciente tiene varias citas agendadas, **Cuando** faltan 24 horas para cada una, **Entonces** el sistema envía un recordatorio independiente por cada cita.

---

### Historia de Usuario 2 - Cancelación de la cita respondiendo al recordatorio (Prioridad: P2)

Como paciente, quiero poder cancelar mi cita respondiendo directamente al mensaje de recordatorio de WhatsApp, para liberar el turno sin tener que llamar o usar otro canal.

**Por qué esta prioridad**: Añade valor incremental sobre la Historia 1: además de recordar, permite que el paciente actúe de inmediato, liberando la franja horaria para otros pacientes y reduciendo aún más las inasistencias sin aviso. Depende de que exista el recordatorio de la Historia 1, pero es una capacidad separable que se puede entregar de forma incremental una vez que el recordatorio ya funciona.

**Prueba independiente**: Puede probarse enviando un recordatorio de prueba, respondiendo "cancelar" desde el número del paciente y verificando que la cita cambia a estado anulado y que la franja horaria vuelve a estar disponible en la agenda.

**Escenarios de Aceptación**:

1. **Dado** que el paciente recibió el recordatorio de WhatsApp, **Cuando** responde que cancela, **Entonces** la cita queda anulada y la franja vuelve a estar disponible en la agenda.
2. **Dado** que el paciente responde al recordatorio con un mensaje que el sistema no reconoce como confirmación ni como cancelación, **Cuando** no puede interpretar la respuesta, **Entonces** la cita permanece sin cambios y se le reenvía al paciente una aclaración de las opciones válidas.
3. **Dado** que el paciente cancela la cita, **Cuando** la cancelación se procesa, **Entonces** el sistema confirma por WhatsApp que la cita fue cancelada exitosamente.

---

### Casos Límite

- ¿Qué ocurre si el paciente responde después de que la cita ya pasó (recordatorio vencido)?
- ¿Qué ocurre si el número de WhatsApp del paciente es inválido o no tiene WhatsApp activo?
- ¿Qué ocurre si el paciente responde "cancelar" pero tiene más de una cita activa al mismo tiempo?
- ¿Qué ocurre si el proveedor externo de mensajería de WhatsApp no está disponible en el momento en que corresponde enviar el recordatorio?
- ¿Qué ocurre si el paciente cancela la cita muy cerca del horario del turno (por ejemplo, minutos antes)?

## Requisitos *(obligatorio)*

### Requisitos Funcionales

- **FR-001**: El sistema DEBE enviar un mensaje de WhatsApp de recordatorio a cada paciente con una cita agendada, exactamente 24 horas antes del horario del turno.
- **FR-002**: El mensaje de recordatorio DEBE incluir la fecha, la hora y el nombre de la médico o el médico tratante.
- **FR-003**: El sistema DEBE permitir que el paciente responda al mensaje de recordatorio para cancelar la cita. El sistema DEBE reconocer la cancelación únicamente mediante una palabra clave exacta (ej. "CANCELAR", sin distinguir mayúsculas/minúsculas ni espacios extremos); cualquier otra respuesta se trata como no reconocida (ver FR-003a).
- **FR-003a**: Cuando la respuesta del paciente no coincide con la palabra clave de cancelación, el sistema DEBE tratarla como no reconocida y reenviar al paciente una aclaración de las opciones válidas, sin modificar el estado de la cita.
- **FR-004**: Cuando el paciente confirma la cancelación por WhatsApp, el sistema DEBE anular la cita y liberar la franja horaria en la agenda para que quede disponible para otros pacientes.
- **FR-005**: El sistema DEBE confirmar al paciente, por WhatsApp, que la cancelación fue procesada exitosamente.
- **FR-006**: El sistema DEBE registrar cada intento de envío de recordatorio (enviado, fallido, sin número válido) para trazabilidad y auditoría.
- **FR-007**: El sistema NO DEBE enviar un recordatorio para una cita que ya fue cancelada previamente.
- **FR-008**: El sistema DEBE aceptar la cancelación por WhatsApp únicamente hasta 2 horas antes del horario del turno. Si el paciente responde con la palabra clave de cancelación después de ese límite, el sistema DEBE rechazar la cancelación por este canal e indicarle que contacte directamente al centro de salud.
- **FR-009**: Cuando un paciente tiene más de una cita activa con recordatorio enviado, el sistema DEBE asociar la respuesta de cancelación exclusivamente a la cita cuyo recordatorio originó ese hilo/conversación de WhatsApp, sin requerir confirmación adicional sobre cuál cita cancelar.

### Entidades Clave

- **Cita**: representa un turno agendado entre un paciente y una médica/médico; incluye fecha, hora, franja horaria y estado (agendada, recordatorio enviado, cancelada, completada).
- **Paciente**: persona que solicita atención; incluye nombre y número de WhatsApp de contacto.
- **Médica/Médico**: profesional de salud asignado a la cita; su nombre se incluye en el recordatorio.
- **Recordatorio**: mensaje de WhatsApp asociado a una cita específica; incluye canal, estado de envío (enviado, fallido, respondido) y marca de tiempo de envío.

## Criterios de Éxito *(obligatorio)*

### Resultados Medibles

- **SC-001**: Al menos el 95% de los recordatorios se envían dentro de la ventana de 24 horas ± 15 minutos antes de la cita correspondiente.
- **SC-002**: La tasa de inasistencias sin previo aviso se reduce en al menos un 30% dentro de los primeros 3 meses de uso de la funcionalidad.
- **SC-003**: Un paciente puede cancelar su cita respondiendo al recordatorio en menos de 1 minuto desde que lo recibe, sin necesidad de usar otro canal de contacto.
- **SC-004**: El 100% de las franjas horarias liberadas por una cancelación vía WhatsApp quedan disponibles en la agenda en menos de 5 minutos desde la confirmación de la cancelación.

## Suposiciones

- Se asume que el sistema ya cuenta con el número de WhatsApp válido del paciente registrado al momento de agendar la cita.
- Se asume que existe (o se integrará) un proveedor de mensajería de WhatsApp Business capaz de enviar y recibir mensajes en nombre del centro de salud.
- Se asume un reintento automático (hasta 3 intentos con espaciamiento creciente) si el envío inicial del recordatorio falla por una causa temporal del proveedor, antes de marcarlo como fallido y notificar al área administrativa.
- Queda fuera del alcance de esta versión la reprogramación de la cita a otro horario respondiendo por WhatsApp; esta especificación solo cubre el envío del recordatorio y la cancelación. La reprogramación podría abordarse en una historia de usuario futura.
