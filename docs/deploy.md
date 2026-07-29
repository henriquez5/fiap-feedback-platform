# Deploy detalhado

## Pre-requisitos

- assinatura Azure com permissao para criar recursos;
- Azure CLI;
- JDK 21;
- Maven 3.9+;
- GitHub repository publico;
- Azure Functions Core Tools para teste local.

## 1. Infraestrutura

```bash
az login
az account set --subscription <SUBSCRIPTION_ID>
az group create --name rg-fiapfeedback --location brazilsouth
az deployment group create \
  --resource-group rg-fiapfeedback \
  --template-file infra/main.bicep \
  --parameters @infra/main.parameters.json \
  --name main
```

Anote os outputs `functionAppName` e `keyVaultName`.

## 2. ACS Email

No Azure Portal:

1. crie `Communication Services`;
2. crie `Email Communication Services`;
3. provisione o dominio gerenciado ou valide um dominio proprio;
4. conecte o dominio ao Communication Services;
5. copie a connection string e o MailFrom address.

Cadastre os segredos no Key Vault e altere `EMAIL_PROVIDER` para `azure`, conforme o README.

## 3. Build e deploy do codigo

```bash
mvn clean verify
mvn -DfunctionAppName=<FUNCTION_APP_NAME> package
mvn -DfunctionAppName=<FUNCTION_APP_NAME> azure-functions:deploy
```

## 4. Obter a Function Key

```bash
az functionapp function keys list \
  --resource-group rg-fiapfeedback \
  --name <FUNCTION_APP_NAME> \
  --function-name receiveFeedback
```

Use `default` na Postman Collection.

## 5. GitHub Actions

Crie uma App Registration ou use uma identidade federada de deploy. Cadastre:

- `AZURE_CLIENT_ID`;
- `AZURE_TENANT_ID`;
- `AZURE_SUBSCRIPTION_ID`.

Execute `Deploy Azure` em **Actions > Run workflow**.

## 6. Smoke test

```bash
curl -X POST "https://<FUNCTION_APP_NAME>.azurewebsites.net/api/avaliacao?code=<FUNCTION_KEY>" \
  -H "Content-Type: application/json" \
  -d '{"descricao":"Teste depois do deploy","nota":9}'
```

## 7. Limpeza

```bash
az group delete --name rg-fiapfeedback --yes --no-wait
```
