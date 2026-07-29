targetScope = 'resourceGroup'

@description('Prefixo curto usado nos nomes dos recursos. Use apenas letras e numeros.')
param appNamePrefix string = 'fiapfeedback'

@description('Regiao principal dos recursos.')
param location string = resourceGroup().location

@description('Origem permitida no CORS. Para testes com Postman, mantenha vazio.')
param allowedCorsOrigin string = ''

@description('Provedor de e-mail. Use log inicialmente e altere para azure apos configurar o ACS.')
@allowed([
  'log'
  'azure'
])
param emailProvider string = 'log'

@description('Endereco remetente validado no Azure Communication Services Email.')
param acsEmailSender string = 'DoNotReply@configure-seu-dominio.azurecomm.net'

@description('CRON NCRONTAB: segunda-feira, 08:00 no horario de Brasilia = 11:00 UTC.')
param weeklyReportCron string = '0 0 11 * * 1'

var suffix = uniqueString(subscription().subscriptionId, resourceGroup().id)
var storageName = toLower(take('${appNamePrefix}${suffix}', 24))
var cosmosName = toLower(take('${appNamePrefix}-cosmos-${suffix}', 44))
var planName = '${appNamePrefix}-plan-${suffix}'
var functionAppName = toLower(take('${appNamePrefix}-func-${suffix}', 60))
var workspaceName = '${appNamePrefix}-law-${suffix}'
var appInsightsName = '${appNamePrefix}-appi-${suffix}'
var keyVaultName = toLower(take('${appNamePrefix}-kv-${suffix}', 24))
var databaseName = 'feedbackdb'
var containerName = 'feedbacks'
var queueName = 'email-notifications'
var deploymentStorageContainerName = 'app-package-${take(suffix, 13)}'

resource storage 'Microsoft.Storage/storageAccounts@2023-05-01' = {
  name: storageName
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    accessTier: 'Hot'
    allowBlobPublicAccess: false
    allowSharedKeyAccess: true
    minimumTlsVersion: 'TLS1_2'
    supportsHttpsTrafficOnly: true
    publicNetworkAccess: 'Enabled'
  }
  tags: {
    project: 'fiap-tech-challenge-fase-4'
    environment: 'academic'
    managedBy: 'bicep'
  }
}

resource queueService 'Microsoft.Storage/storageAccounts/queueServices@2023-05-01' = {
  parent: storage
  name: 'default'
}

resource notificationQueue 'Microsoft.Storage/storageAccounts/queueServices/queues@2023-05-01' = {
  parent: queueService
  name: queueName
}

resource blobService 'Microsoft.Storage/storageAccounts/blobServices@2023-05-01' = {
  parent: storage
  name: 'default'
}

resource deploymentContainer 'Microsoft.Storage/storageAccounts/blobServices/containers@2023-05-01' = {
  parent: blobService
  name: deploymentStorageContainerName

  properties: {
    publicAccess: 'None'
  }
}

resource cosmos 'Microsoft.DocumentDB/databaseAccounts@2024-05-15' = {
  name: cosmosName
  location: location
  kind: 'GlobalDocumentDB'
  properties: {
    databaseAccountOfferType: 'Standard'
    consistencyPolicy: {
      defaultConsistencyLevel: 'Session'
    }
    locations: [
      {
        locationName: location
        failoverPriority: 0
        isZoneRedundant: false
      }
    ]
    capabilities: [
      {
        name: 'EnableServerless'
      }
    ]
    publicNetworkAccess: 'Enabled'
    minimalTlsVersion: 'Tls12'
    disableLocalAuth: false
  }
  tags: {
    project: 'fiap-tech-challenge-fase-4'
    environment: 'academic'
    managedBy: 'bicep'
  }
}

resource database 'Microsoft.DocumentDB/databaseAccounts/sqlDatabases@2024-05-15' = {
  parent: cosmos
  name: databaseName
  properties: {
    resource: {
      id: databaseName
    }
  }
}

resource container 'Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers@2024-05-15' = {
  parent: database
  name: containerName
  properties: {
    resource: {
      id: containerName
      partitionKey: {
        paths: [
          '/partitionKey'
        ]
        kind: 'Hash'
        version: 2
      }
      indexingPolicy: {
        indexingMode: 'consistent'
        automatic: true
        includedPaths: [
          {
            path: '/*'
          }
        ]
        excludedPaths: [
          {
            path: '/"_etag"/?'
          }
        ]
      }
    }
  }
}

resource logAnalytics 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: workspaceName
  location: location
  properties: {
    retentionInDays: 30
    features: {
      enableLogAccessUsingOnlyResourcePermissions: true
    }
  }
  tags: {
    project: 'fiap-tech-challenge-fase-4'
    environment: 'academic'
    managedBy: 'bicep'
  }
}

resource appInsights 'Microsoft.Insights/components@2020-02-02' = {
  name: appInsightsName
  location: location
  kind: 'web'
  properties: {
    Application_Type: 'web'
    WorkspaceResourceId: logAnalytics.id
    RetentionInDays: 30
    IngestionMode: 'LogAnalytics'
  }
  tags: {
    project: 'fiap-tech-challenge-fase-4'
    environment: 'academic'
    managedBy: 'bicep'
  }
}

resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: keyVaultName
  location: location
  properties: {
    tenantId: tenant().tenantId
    sku: {
      family: 'A'
      name: 'standard'
    }
    enableRbacAuthorization: true
    enabledForDeployment: false
    enabledForTemplateDeployment: true
    enableSoftDelete: true
    softDeleteRetentionInDays: 7
    publicNetworkAccess: 'Enabled'
  }
  tags: {
    project: 'fiap-tech-challenge-fase-4'
    environment: 'academic'
    managedBy: 'bicep'
  }
}

resource plan 'Microsoft.Web/serverfarms@2024-04-01' = {
  name: planName
  location: location
  kind: 'functionapp'

  sku: {
    name: 'FC1'
    tier: 'FlexConsumption'
  }

  properties: {
    reserved: true
  }

  tags: {
    project: 'fiap-tech-challenge-fase-4'
    environment: 'academic'
    managedBy: 'bicep'
  }
}

var storageConnectionString = 'DefaultEndpointsProtocol=https;AccountName=${storage.name};AccountKey=${listKeys(storage.id, '2023-05-01').keys[0].value};EndpointSuffix=${environment().suffixes.storage}'
var cosmosConnectionString = listConnectionStrings(cosmos.id, '2024-05-15').connectionStrings[0].connectionString
var appSettings = union([
  {
    name: 'FUNCTIONS_EXTENSION_VERSION'
    value: '~4'
  }
  {
    name: 'JAVA_OPTS'
    value: '-Djava.net.preferIPv4Stack=true'
  }
  {
    name: 'AzureWebJobsStorage'
    value: storageConnectionString
  }
  {
    name: 'APPLICATIONINSIGHTS_CONNECTION_STRING'
    value: appInsights.properties.ConnectionString
  }
  {
    name: 'COSMOS_CONNECTION'
    value: cosmosConnectionString
  }
  {
    name: 'COSMOS_DATABASE_NAME'
    value: databaseName
  }
  {
    name: 'COSMOS_CONTAINER_NAME'
    value: containerName
  }
  {
    name: 'NOTIFICATION_QUEUE_NAME'
    value: queueName
  }
  {
    name: 'WEEKLY_REPORT_CRON'
    value: weeklyReportCron
  }
  {
    name: 'EMAIL_PROVIDER'
    value: emailProvider
  }
  {
    name: 'ACS_EMAIL_CONNECTION_STRING'
    value: '@Microsoft.KeyVault(VaultUri=${keyVault.properties.vaultUri};SecretName=acs-email-connection-string)'
  }
  {
    name: 'ADMIN_EMAIL'
    value: '@Microsoft.KeyVault(VaultUri=${keyVault.properties.vaultUri};SecretName=admin-email)'
  }
  {
    name: 'ACS_EMAIL_SENDER'
    value: acsEmailSender
  }
], empty(allowedCorsOrigin) ? [] : [
  {
    name: 'ALLOWED_CORS_ORIGIN'
    value: allowedCorsOrigin
  }
])

resource functionApp 'Microsoft.Web/sites@2024-04-01' = {
  name: functionAppName
  location: location
  kind: 'functionapp,linux'

  identity: {
    type: 'SystemAssigned'
  }

  properties: {
    serverFarmId: plan.id
    httpsOnly: true
    publicNetworkAccess: 'Enabled'

    siteConfig: {
      minTlsVersion: '1.2'
      appSettings: appSettings

      cors: empty(allowedCorsOrigin) ? null : {
        allowedOrigins: [
          allowedCorsOrigin
        ]
        supportCredentials: false
      }
    }

    functionAppConfig: {
      deployment: {
        storage: {
          type: 'blobContainer'
          value: '${storage.properties.primaryEndpoints.blob}${deploymentStorageContainerName}'

          authentication: {
            type: 'StorageAccountConnectionString'
            storageAccountConnectionStringName: 'AzureWebJobsStorage'
          }
        }
      }

      scaleAndConcurrency: {
        maximumInstanceCount: 40
        instanceMemoryMB: 2048
      }

      runtime: {
        name: 'java'
        version: '21'
      }
    }
  }

  tags: {
    project: 'fiap-tech-challenge-fase-4'
    environment: 'academic'
    managedBy: 'bicep'
  }
}

resource keyVaultSecretsUser 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(keyVault.id, functionApp.id, 'key-vault-secrets-user')
  scope: keyVault
  properties: {
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '4633458b-17de-408a-b874-0445c86b69e6')
    principalId: functionApp.identity.principalId
    principalType: 'ServicePrincipal'
  }
}

output functionAppName string = functionApp.name
output functionBaseUrl string = 'https://${functionApp.properties.defaultHostName}'
output keyVaultName string = keyVault.name
output cosmosAccountName string = cosmos.name
output storageAccountName string = storage.name
output applicationInsightsName string = appInsights.name
