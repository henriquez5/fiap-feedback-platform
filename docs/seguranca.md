# Seguranca e governanca

## Controles implementados

1. **HTTPS obrigatorio** na Function App.
2. **TLS minimo 1.2** em Function App, Storage e Cosmos DB.
3. **FTPS desabilitado**.
4. **Blob publico desabilitado**.
5. **Function Key** no endpoint HTTP.
6. **Validacao de entrada** e limite de tamanho da descricao.
7. **Escape de HTML** nos e-mails para impedir injecao de markup.
8. **Segredos fora do repositorio**.
9. **Azure Key Vault** para connection string do ACS e e-mail administrativo.
10. **Managed Identity** da Function App com papel `Key Vault Secrets User`.
11. **RBAC** no Key Vault e principio do menor privilegio.
12. **Tags de governanca** em todos os recursos.
13. **Correlation ID** sem gravar segredos ou tokens nos logs.
14. `local.settings.json` ignorado pelo Git.

## Segredos

Nunca versionar:

- connection strings;
- chaves de Function;
- e-mail real do administrador, quando considerado dado interno;
- credenciais de deploy.

No GitHub, o workflow usa federacao OIDC e os identificadores:

- `AZURE_CLIENT_ID`;
- `AZURE_TENANT_ID`;
- `AZURE_SUBSCRIPTION_ID`.

Nao e necessario salvar senha de service principal.

## Evolucao recomendada para producao

- colocar API Management na frente da Function;
- autenticar usuarios por Microsoft Entra ID;
- usar conexoes identity-based para Storage e Cosmos DB;
- usar Private Endpoints e desabilitar acesso publico;
- habilitar Microsoft Defender for Cloud;
- criar politica de retencao e descarte de feedbacks;
- classificar descricao como dado potencialmente pessoal;
- adicionar rate limiting e WAF;
- implementar idempotencia de notificacoes.

## Governanca de custos

- Cosmos DB no modo serverless;
- Function App no plano Consumption;
- Storage Standard LRS;
- Log Analytics com retencao de 30 dias;
- recursos agrupados em um unico Resource Group;
- remocao completa com um unico comando apos a avaliacao.
