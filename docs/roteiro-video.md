# Roteiro sugerido para o video

Duracao recomendada: **8 a 12 minutos**.

## 1. Abertura — 30 segundos

> Este projeto implementa uma plataforma serverless de feedbacks em Java 21 no Microsoft Azure. A solucao recebe avaliacoes, notifica automaticamente feedbacks criticos e gera um relatorio semanal, com infraestrutura como codigo, CI/CD, seguranca e monitoramento.

## 2. Arquitetura — 1 minuto

Mostrar o diagrama do README e explicar:

- Azure Functions;
- Cosmos DB serverless;
- Queue Storage;
- ACS Email;
- Application Insights;
- Key Vault;
- separacao de responsabilidades.

## 3. Repositorio — 1 minuto

Mostrar:

- codigo das tres funcoes;
- testes;
- Bicep;
- workflows;
- Postman;
- documentacao.

Citar a regra de urgencia e justificar que o enunciado nao definiu os intervalos.

## 4. Recursos no Azure — 1 minuto

No Resource Group, mostrar:

- Function App;
- Cosmos DB;
- Storage Account e fila;
- Application Insights;
- Key Vault.

Abrir a configuracao da Function App e mostrar Java 21, HTTPS, TLS e app settings sem revelar segredos.

## 5. Feedback normal — 1 minuto

No Postman:

```json
{"descricao":"A aula foi clara e objetiva","nota":9}
```

Mostrar `201`, urgencia `NORMAL` e documento no Cosmos DB.

## 6. Feedback invalido — 30 segundos

```json
{"descricao":"","nota":11}
```

Mostrar `400`, lista de erros e correlation ID.

## 7. Feedback critico — 1 minuto

```json
{"descricao":"O laboratorio falhou e impediu a conclusao da aula","nota":2}
```

Mostrar:

- `201` com `CRITICA`;
- mensagem sendo processada na funcao de e-mail;
- e-mail recebido com descricao, urgencia e data.

## 8. Relatorio — 1 minuto

Antes da gravacao, alterar temporariamente `WEEKLY_REPORT_CRON` para:

```text
0 */2 * * * *
```

Mostrar a execucao do Timer Trigger e o e-mail com:

- media;
- quantidade por dia;
- quantidade por urgencia;
- detalhamento.

Restaurar o CRON semanal e dizer isso no video.

## 9. Monitoramento — 1 minuto

No Application Insights:

- executar uma consulta KQL;
- mostrar `feedback.created`, `weekly_report.generated` e `email.sent`;
- mostrar request, duracao e sucesso.

## 10. CI/CD e encerramento — 1 minuto

Mostrar um workflow verde e explicar:

- testes;
- package;
- Bicep;
- deploy automatizado.

Encerrar reforcando que a arquitetura e serverless, monitorada, segura e reproduzivel.

## Cuidados

- nao exibir connection strings ou chaves;
- gravar com zoom legivel;
- deixar Postman, e-mail e portal abertos antes de iniciar;
- testar o fluxo completo antes da gravacao;
- colocar o link do repositorio na entrega;
- confirmar que o repositorio esta publico.
