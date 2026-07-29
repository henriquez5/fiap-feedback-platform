# Arquitetura e decisoes tecnicas

## 1. Modelo de cloud escolhido

A solucao utiliza **nuvem publica Microsoft Azure** e combina:

- **FaaS:** Azure Functions para executar codigo sob demanda;
- **BaaS/PaaS:** Cosmos DB, Queue Storage, Communication Services, Key Vault e Application Insights;
- **modelo de consumo:** recursos dimensionados de acordo com requisicoes e execucoes.

O modelo reduz a necessidade de administrar servidores, sistema operacional, patches e escalabilidade manual. Essa escolha atende diretamente ao requisito de serverless e e adequada a uma carga academica intermitente.

## 2. Fluxos

### Recebimento normal

1. Cliente envia `POST /api/avaliacao`.
2. A funcao valida descricao e nota.
3. A nota e convertida em urgencia.
4. O documento e salvo no Cosmos DB.
5. A resposta `201` retorna o identificador e a classificacao.

### Feedback critico

1. O fluxo de recebimento identifica nota de 0 a 3.
2. Uma notificacao e publicada na Queue Storage.
3. `dispatchEmailNotification` consome a mensagem.
4. Azure Communication Services envia o e-mail ao administrador.
5. Em falha, a Queue Trigger tenta novamente; apos cinco falhas, a mensagem vai para a poison queue.

### Relatorio semanal

1. `generateWeeklyReport` e disparada por Timer Trigger.
2. A funcao consulta o container de feedbacks.
3. Filtra o periodo de sete dias.
4. Calcula media, quantidade por dia e quantidade por urgencia.
5. Monta o detalhamento com descricao, urgencia e data.
6. Publica o comando de e-mail na mesma fila.
7. O dispatcher envia o relatorio.

## 3. Responsabilidade unica

A arquitetura nao concentra persistencia, agregacao e envio de e-mail em uma unica funcao. Cada componente possui um motivo especifico para mudar:

- mudanca no contrato HTTP afeta o receiver;
- mudanca no calculo semanal afeta o report generator;
- mudanca de provedor de e-mail afeta o dispatcher/adaptador.

## 4. Consistencia e resiliencia

A persistencia e o output de fila usam bindings da Function. A notificacao e assíncrona, portanto uma lentidao no provedor de e-mail nao aumenta o tempo da requisicao HTTP.

A solucao e **at-least-once**: uma mensagem pode ser processada novamente em caso de falha. O e-mail possui `notification.id` e `correlationId`, permitindo rastreio e futura implementacao de idempotencia persistente.

## 5. Particionamento

O container usa `/partitionKey`. Para o escopo pequeno do desafio, o valor e `feedback`. Em uma evolucao multi-curso, a particao deve ser alterada para `courseId` ou `classId` para distribuir carga e permitir relatorios por curso.

## 6. Trade-offs

- O Cosmos DB Input Binding consulta os documentos e o codigo filtra sete dias. E simples para a demonstracao e mantem o foco no desafio. Em alto volume, a consulta deve receber limites temporais no proprio SQL e usar indices compostos.
- O ACS Email exige configuracao de dominio fora do fluxo principal do Bicep. Essa etapa e documentada e os segredos ficam no Key Vault.
- A Function Key fornece uma camada basica de autenticacao. Em producao, recomenda-se Microsoft Entra ID e API Management.
