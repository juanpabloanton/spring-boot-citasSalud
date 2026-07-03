# language: es
Característica: Recordatorio automático de cita por WhatsApp
  Como paciente
  quiero recibir un recordatorio automático por WhatsApp antes de mi cita
  para no olvidarla ni generar una inasistencia sin previo aviso

  Escenario: Envío exitoso del recordatorio 24 horas antes del turno
    Dado que el paciente "Juan Pérez" con WhatsApp "573001234567" tiene una cita agendada con la médica "Dra. Ana Torres" para dentro de 24 horas
    Cuando el sistema ejecuta el envío de recordatorios pendientes
    Entonces se envía un mensaje de WhatsApp con la fecha, la hora y el nombre de la médica al paciente "Juan Pérez"
    Y la cita del paciente "Juan Pérez" queda en estado "RECORDATORIO_ENVIADO"

  Escenario: Paciente sin número de WhatsApp válido registrado
    Dado que el paciente "Carlos Ruiz" sin número de WhatsApp válido tiene una cita agendada para dentro de 24 horas
    Cuando el sistema ejecuta el envío de recordatorios pendientes
    Entonces el sistema registra el intento fallido por número inválido para el paciente "Carlos Ruiz"
    Y la cita del paciente "Carlos Ruiz" permanece en estado "AGENDADA"

  Escenario: Múltiples citas agendadas reciben recordatorios independientes
    Dado que el paciente "Ana Gómez" con WhatsApp "573007654321" tiene 2 citas agendadas para dentro de 24 horas
    Cuando el sistema ejecuta el envío de recordatorios pendientes
    Entonces se envían 2 recordatorios independientes al paciente "Ana Gómez"
