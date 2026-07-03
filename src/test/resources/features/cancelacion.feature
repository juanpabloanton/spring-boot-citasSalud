# language: es
Característica: Cancelación de la cita respondiendo al recordatorio
  Como paciente
  quiero poder cancelar mi cita respondiendo directamente al mensaje de recordatorio de WhatsApp
  para liberar el turno sin tener que llamar o usar otro canal

  Escenario: Cancelación exitosa libera la franja horaria
    Dado que el paciente "Juan Pérez" recibió el recordatorio de WhatsApp de su cita
    Cuando responde "CANCELAR" al recordatorio
    Entonces la cita queda anulada y la franja vuelve a estar disponible en la agenda

  Escenario: Respuesta no reconocida no modifica la cita y reenvía la aclaración
    Dado que el paciente "Carlos Ruiz" recibió el recordatorio de WhatsApp de su cita
    Cuando responde "tal vez despues" al recordatorio
    Entonces la cita permanece sin cambios
    Y se le reenvía al paciente "Carlos Ruiz" una aclaración de las opciones válidas

  Escenario: El sistema confirma por WhatsApp la cancelación exitosa
    Dado que el paciente "Ana Gómez" recibió el recordatorio de WhatsApp de su cita
    Cuando responde "CANCELAR" al recordatorio
    Entonces el sistema confirma por WhatsApp al paciente "Ana Gómez" que la cita fue cancelada exitosamente
