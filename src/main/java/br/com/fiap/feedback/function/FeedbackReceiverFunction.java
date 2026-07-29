package br.com.fiap.feedback.function;

import br.com.fiap.feedback.application.EmailTemplateService;
import br.com.fiap.feedback.application.FeedbackApplicationService;
import br.com.fiap.feedback.application.ValidationException;
import br.com.fiap.feedback.domain.EmailNotification;
import br.com.fiap.feedback.domain.ErrorResponse;
import br.com.fiap.feedback.domain.Feedback;
import br.com.fiap.feedback.domain.FeedbackRequest;
import br.com.fiap.feedback.domain.FeedbackResponse;
import br.com.fiap.feedback.domain.Urgency;
import br.com.fiap.feedback.infrastructure.JsonSupport;
import br.com.fiap.feedback.infrastructure.StructuredLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.CosmosDBOutput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.QueueOutput;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class FeedbackReceiverFunction {
    private static final ObjectMapper MAPPER = JsonSupport.mapper();
    private static final FeedbackApplicationService SERVICE = FeedbackApplicationService.production();
    private static final EmailTemplateService EMAIL_TEMPLATE = new EmailTemplateService();

    @FunctionName("receiveFeedback")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "request",
                    methods = {HttpMethod.POST},
                    route = "avaliacao",
                    authLevel = AuthorizationLevel.FUNCTION
            ) HttpRequestMessage<Optional<String>> request,
            @CosmosDBOutput(
                    name = "feedbackDocument",
                    databaseName = "%COSMOS_DATABASE_NAME%",
                    containerName = "%COSMOS_CONTAINER_NAME%",
                    connection = "COSMOS_CONNECTION"
            ) OutputBinding<String> feedbackDocument,
            @QueueOutput(
                    name = "notificationMessage",
                    queueName = "%NOTIFICATION_QUEUE_NAME%",
                    connection = "AzureWebJobsStorage"
            ) OutputBinding<String> notificationMessage,
            ExecutionContext context
    ) {
        String correlationId = request.getHeaders().getOrDefault("x-correlation-id", UUID.randomUUID().toString());

        try {
            String body = request.getBody().orElseThrow(() ->
                    new ValidationException(List.of("O corpo da requisicao e obrigatorio.")));
            FeedbackRequest payload = MAPPER.readValue(body, FeedbackRequest.class);
            Feedback feedback = SERVICE.create(payload);

            feedbackDocument.setValue(MAPPER.writeValueAsString(feedback));

            if (feedback.urgencia() == Urgency.CRITICA) {
                EmailNotification notification = EMAIL_TEMPLATE.criticalFeedback(
                        feedback,
                        correlationId,
                        Instant.now()
                );
                notificationMessage.setValue(MAPPER.writeValueAsString(notification));
            }

            StructuredLog.info(context, "feedback.created", correlationId,
                    "feedbackId=" + feedback.id() + " urgency=" + feedback.urgencia());

            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("X-Correlation-Id", correlationId)
                    .body(MAPPER.writeValueAsString(FeedbackResponse.from(feedback)))
                    .build();
        } catch (ValidationException exception) {
            StructuredLog.warning(context, "feedback.validation_failed", correlationId,
                    String.join("; ", exception.details()));
            return errorResponse(request, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    exception.getMessage(), exception.details(), correlationId);
        } catch (JsonProcessingException exception) {
            StructuredLog.warning(context, "feedback.invalid_json", correlationId, exception.getOriginalMessage());
            return errorResponse(request, HttpStatus.BAD_REQUEST, "INVALID_JSON",
                    "O JSON enviado e invalido.", List.of(exception.getOriginalMessage()), correlationId);
        } catch (Exception exception) {
            StructuredLog.error(context, "feedback.unexpected_error", correlationId, exception);
            return errorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "Nao foi possivel processar o feedback.", List.of(), correlationId);
        }
    }

    private HttpResponseMessage errorResponse(
            HttpRequestMessage<?> request,
            HttpStatus status,
            String code,
            String message,
            List<String> details,
            String correlationId
    ) {
        try {
            ErrorResponse response = new ErrorResponse(
                    code, message, details, correlationId, Instant.now()
            );
            return request.createResponseBuilder(status)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("X-Correlation-Id", correlationId)
                    .body(MAPPER.writeValueAsString(response))
                    .build();
        } catch (JsonProcessingException exception) {
            return request.createResponseBuilder(status)
                    .header("X-Correlation-Id", correlationId)
                    .body(message)
                    .build();
        }
    }
}
