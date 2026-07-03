# Modelo de Datos: Recordatorio de Cita por WhatsApp

## Cita

Representa un turno agendado entre un paciente y una médica/médico.

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | Identificador único |
| `pacienteId` | UUID | Referencia a `Paciente` |
| `medicoId` | UUID | Referencia a `Medico` |
| `fechaHora` | Instant / ZonedDateTime | Fecha y hora del turno |
| `estado` | `EstadoCita` (enum) | Ver transiciones abajo |
| `recordatorioEnviadoEn` | Instant \| null | Marca de tiempo del envío del recordatorio; `null` si aún no se envió |

### `EstadoCita` (enum)

- `AGENDADA`
- `RECORDATORIO_ENVIADO`
- `CANCELADA`
- `COMPLETADA`

### Transiciones de estado

```text
AGENDADA ──(EnviarRecordatorioUseCase, 24h antes)──> RECORDATORIO_ENVIADO
RECORDATORIO_ENVIADO ──(CancelarCitaUseCase, respuesta "CANCELAR" ≤2h antes)──> CANCELADA
RECORDATORIO_ENVIADO ──(paso del horario del turno sin cancelación)──> COMPLETADA
AGENDADA ──(paso del horario del turno sin cancelación)──> COMPLETADA
```

### Reglas de validación (derivadas de los Requisitos Funcionales)

- FR-001/FR-007: solo se dispara el envío de recordatorio para citas en estado `AGENDADA`; una cita `CANCELADA` nunca recibe recordatorio.
- FR-008: `CancelarCitaUseCase` solo se ejecuta si `fechaHora - ahora >= 2 horas`; en caso contrario se rechaza y se responde al paciente indicando que contacte al centro de salud.
- FR-009: la cancelación entrante se resuelve por la `Cita` asociada al `Recordatorio` cuyo hilo/conversación originó la respuesta (ver `Recordatorio.citaId`), nunca por búsqueda ambigua entre todas las citas activas del paciente.

## Paciente

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | Identificador único |
| `nombre` | String | Nombre completo, usado en mensajes de confirmación |
| `numeroWhatsapp` | String | Formato E.164; puede ser inválido/ausente (ver Recordatorio.estadoEnvio = `SIN_NUMERO_VALIDO`) |

## Medico

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | Identificador único |
| `nombre` | String | Incluido en el mensaje de recordatorio (FR-002) |

## Recordatorio

Mensaje de WhatsApp asociado a una cita específica; cada hilo de conversación de WhatsApp queda ligado a un único `Recordatorio`/`Cita` (FR-009).

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | Identificador único |
| `citaId` | UUID | Referencia a `Cita` (relación 1 a 1 por envío) |
| `canal` | String constante | `"WHATSAPP"` |
| `estadoEnvio` | `EstadoEnvioRecordatorio` (enum) | Ver abajo |
| `enviadoEn` | Instant \| null | Marca de tiempo del envío exitoso |
| `intentos` | int | Número de intentos de envío realizados (máx. 3, ver Suposiciones de spec.md) |
| `mensajeProveedorId` | String \| null | Identificador del mensaje devuelto por el proveedor de WhatsApp, usado para correlacionar la respuesta entrante con esta `Cita` |

### `EstadoEnvioRecordatorio` (enum)

- `PENDIENTE`
- `ENVIADO`
- `FALLIDO`
- `SIN_NUMERO_VALIDO`
- `RESPONDIDO_CANCELACION`
- `RESPONDIDO_NO_RECONOCIDO`

## Relaciones

```text
Paciente (1) ──── (N) Cita
Medico   (1) ──── (N) Cita
Cita     (1) ──── (1) Recordatorio   [por cada envío realizado]
```
