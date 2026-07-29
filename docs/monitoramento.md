# Monitoramento e observabilidade

## Telemetria

O Application Insights recebe automaticamente:

- requests HTTP;
- duracao e codigo de resposta;
- execucoes das funcoes;
- dependencias;
- excecoes;
- traces estruturados.

Os logs usam os campos textuais `event`, `correlationId` e `details`, permitindo cruzar o recebimento, a criacao do relatorio e o envio de e-mail.

## Eventos relevantes

| Evento | Significado |
|---|---|
| `feedback.created` | Feedback validado e produzido para persistencia. |
| `feedback.validation_failed` | Requisicao rejeitada por regra de negocio. |
| `weekly_report.generated` | Relatorio calculado e enfileirado. |
| `weekly_report.failed` | Falha na consulta ou agregacao. |
| `email.sent` | Provedor aceitou o envio. |
| `email.failed` | Falha no dispatcher; a fila fara retry. |

## KQL para o video

### Ultimos feedbacks criados

```kusto
traces
| where message contains "event=feedback.created"
| project timestamp, message, operation_Id
| order by timestamp desc
```

### Falhas

```kusto
traces
| where severityLevel >= 3
   or message contains "event=email.failed"
   or message contains "event=weekly_report.failed"
| project timestamp, severityLevel, message, operation_Id
| order by timestamp desc
```

### Disponibilidade do endpoint

```kusto
requests
| where name contains "receiveFeedback"
| summarize requests=count(), failures=countif(success == false), p95=percentile(duration, 95) by bin(timestamp, 15m)
| order by timestamp desc
```

## Alertas sugeridos

1. Mais de tres falhas no endpoint em cinco minutos.
2. Qualquer `email.failed` em quinze minutos.
3. Mensagem presente em `email-notifications-poison`.
4. Ausencia de `weekly_report.generated` por mais de oito dias.

## Evidencias

Capturar no video:

- Live Metrics;
- execucao das tres funcoes;
- trace com correlation ID;
- request `201`;
- mensagem de erro `400`;
- envio do e-mail;
- execucao do Timer Trigger.
