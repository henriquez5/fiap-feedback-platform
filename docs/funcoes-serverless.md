# Documentacao das funcoes serverless

## `receiveFeedback`

- **Trigger:** HTTP.
- **Metodo:** POST.
- **Rota:** `/api/avaliacao`.
- **Autorizacao:** Function Key.
- **Outputs:** Cosmos DB e, condicionalmente, Queue Storage.

### Entrada

```json
{
  "descricao": "string obrigatoria",
  "nota": 0
}
```

### Respostas

- `201`: feedback criado;
- `400`: validacao ou JSON invalido;
- `500`: erro inesperado.

### Logs

- `feedback.created`;
- `feedback.validation_failed`;
- `feedback.invalid_json`;
- `feedback.unexpected_error`.

## `generateWeeklyReport`

- **Trigger:** Timer.
- **Agenda:** `WEEKLY_REPORT_CRON`.
- **Input:** consulta ao Cosmos DB.
- **Output:** Queue Storage.

Indicadores:

- media das notas;
- total de feedbacks;
- quantidade por dia;
- quantidade por urgencia;
- descricao, urgencia e data de cada feedback.

Logs:

- `weekly_report.generated`;
- `weekly_report.failed`.

## `dispatchEmailNotification`

- **Trigger:** Queue Storage.
- **Fila:** `NOTIFICATION_QUEUE_NAME`.
- **Provider local:** `log`.
- **Provider cloud:** `azure`.

Variaveis exigidas no modo Azure:

- `ACS_EMAIL_CONNECTION_STRING`;
- `ACS_EMAIL_SENDER`;
- `ADMIN_EMAIL`.

Logs:

- `email.sent`;
- `email.failed`.

Em caso de excecao, a funcao relanca o erro para preservar o retry automatico da Queue Trigger.
