# Checklist orientado pela rubrica

Use esta matriz na revisao final e na gravacao do video.

| Criterio de avaliacao | Onde esta implementado | Evidencia a mostrar |
|---|---|---|
| Explicacao do modelo de cloud | `README.md` e `docs/arquitetura.md` | Diagrama e justificativa FaaS/BaaS/Consumption. |
| Componentes da solucao | Bicep, diagrama e Resource Group | Function App, Cosmos DB, Storage Queue, ACS Email, Key Vault e App Insights. |
| Funcionamento correto | Tres funcoes e Postman Collection | Fluxos normal, invalido, critico e relatorio. |
| Qualidade do codigo | Camadas `domain`, `application`, `infrastructure`, testes | Testes verdes, validacao, logs e tratamento de erros. |
| Documentacao | Pasta `docs` e README | Abrir os documentos rapidamente no video. |
| Arquitetura da solucao | `docs/arquitetura.md` | Explicar responsabilidades e fluxo assincrono. |
| Instrucoes de deploy | `docs/deploy.md`, Bicep e Actions | Workflow verde e outputs do deployment. |
| Configuracao do monitoramento | `docs/monitoramento.md`, `host.json` | KQL, requests, traces e exceptions. |
| Documentacao das funcoes | `docs/funcoes-serverless.md` | Trigger, input, output e logs de cada funcao. |
| Ambiente cloud e serverless | `infra/main.bicep` | Plano Consumption e Cosmos serverless. |
| Seguranca | `docs/seguranca.md` e Bicep | HTTPS, TLS, Key Vault, RBAC e Function Key. |
| Duas ou mais funcoes com SRP | Tres classes em `function` | Tabela de responsabilidades. |
| Notificacao de problemas criticos | Receiver + Queue + Dispatcher | E-mail real com descricao, urgencia e data. |
| Relatorio semanal | Timer + Report Service | E-mail com media, totais por dia/urgencia e detalhes. |
| Repositorio aberto | GitHub | URL publica sem segredos. |
| Video | `docs/roteiro-video.md` | Link acessivel e demonstracao completa. |

## Bloqueadores antes da entrega

- [ ] Substituir os placeholders do ACS Email.
- [ ] Colocar o e-mail do administrador no Key Vault.
- [ ] Confirmar `EMAIL_PROVIDER=azure`.
- [ ] Testar a function key no Postman.
- [ ] Confirmar o recebimento do e-mail critico.
- [ ] Confirmar o recebimento do relatorio.
- [ ] Restaurar o CRON semanal depois do teste acelerado.
- [ ] Apagar ou ocultar dados sensiveis da gravacao.
- [ ] Confirmar que Actions e testes estao verdes.
- [ ] Confirmar que o repositorio esta publico.
