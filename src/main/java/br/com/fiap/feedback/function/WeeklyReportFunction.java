package br.com.fiap.feedback.function;

import br.com.fiap.feedback.application.EmailTemplateService;
import br.com.fiap.feedback.application.WeeklyReportService;
import br.com.fiap.feedback.domain.EmailNotification;
import br.com.fiap.feedback.domain.Feedback;
import br.com.fiap.feedback.domain.WeeklyReport;
import br.com.fiap.feedback.infrastructure.JsonSupport;
import br.com.fiap.feedback.infrastructure.StructuredLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.CosmosDBInput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueOutput;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import java.util.Arrays;
import java.util.Optional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class WeeklyReportFunction {
    private static final ObjectMapper MAPPER = JsonSupport.mapper();
    private static final WeeklyReportService REPORT_SERVICE = new WeeklyReportService();
    private static final EmailTemplateService EMAIL_TEMPLATE = new EmailTemplateService();

    @FunctionName("generateWeeklyReport")
    public void run(
            @TimerTrigger(
                    name = "timerInfo",
                    schedule = "%WEEKLY_REPORT_CRON%"
            ) String timerInfo,
            @CosmosDBInput(
                    name = "feedbackDocuments",
                    databaseName = "%COSMOS_DATABASE_NAME%",
                    containerName = "%COSMOS_CONTAINER_NAME%",
                    sqlQuery = "SELECT * FROM c WHERE c.partitionKey = 'feedback'",
                    connection = "COSMOS_CONNECTION"
            ) Optional<String> feedbackDocumentsJson,
            @QueueOutput(
                    name = "notificationMessage",
                    queueName = "%NOTIFICATION_QUEUE_NAME%",
                    connection = "AzureWebJobsStorage"
            ) OutputBinding<String> notificationMessage,
            ExecutionContext context
    ) throws Exception {
        String correlationId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        try {
        	String rawDocuments = feedbackDocumentsJson == null
        	        ? "[]"
        	        : feedbackDocumentsJson.orElse("[]");

        	Feedback[] parsedDocuments = MAPPER.readValue(
        	        rawDocuments,
        	        Feedback[].class
        	);

        	List<Feedback> feedbacks = parsedDocuments == null
        	        ? List.of()
        	        : Arrays.asList(parsedDocuments);
            WeeklyReport report = REPORT_SERVICE.generate(feedbacks, now);
            EmailNotification notification = EMAIL_TEMPLATE.weeklyReport(report, correlationId, now);
            notificationMessage.setValue(MAPPER.writeValueAsString(notification));

            StructuredLog.info(context, "weekly_report.generated", correlationId,
                    "total=" + report.totalFeedbacks() + " average=" + report.averageScore());
        } catch (Exception exception) {
            StructuredLog.error(context, "weekly_report.failed", correlationId, exception);
            throw exception;
        }
    }
}
