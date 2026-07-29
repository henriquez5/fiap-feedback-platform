package br.com.fiap.feedback.function;

import br.com.fiap.feedback.domain.EmailNotification;
import br.com.fiap.feedback.infrastructure.JsonSupport;
import br.com.fiap.feedback.infrastructure.StructuredLog;
import br.com.fiap.feedback.infrastructure.email.EmailSender;
import br.com.fiap.feedback.infrastructure.email.EmailSenderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;

public final class EmailDispatcherFunction {
    private static final ObjectMapper MAPPER = JsonSupport.mapper();

    @FunctionName("dispatchEmailNotification")
    public void run(
            @QueueTrigger(
                    name = "notificationMessage",
                    queueName = "%NOTIFICATION_QUEUE_NAME%",
                    connection = "AzureWebJobsStorage"
            ) String notificationMessage,
            ExecutionContext context
    ) throws Exception {
        String correlationId = "not-available";
        try {
            EmailNotification notification = MAPPER.readValue(notificationMessage, EmailNotification.class);
            correlationId = notification.correlationId();
            EmailSender sender = EmailSenderFactory.create(context.getLogger());
            String operationId = sender.send(notification);

            StructuredLog.info(context, "email.sent", correlationId,
                    "type=" + notification.type() + " operationId=" + operationId);
        } catch (Exception exception) {
            StructuredLog.error(context, "email.failed", correlationId, exception);
            throw exception;
        }
    }
}
