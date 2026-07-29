package br.com.fiap.feedback.application;

import br.com.fiap.feedback.domain.EmailNotification;
import br.com.fiap.feedback.domain.Feedback;
import br.com.fiap.feedback.domain.NotificationType;
import br.com.fiap.feedback.domain.Urgency;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailTemplateServiceTest {
    private final EmailTemplateService service = new EmailTemplateService();

    @Test
    void shouldIncludeRequiredCriticalFeedbackFieldsAndEscapeHtml() {
        Feedback feedback = new Feedback(
                "id-1",
                Feedback.PARTITION_KEY,
                "Erro <critico>",
                1,
                Urgency.CRITICA,
                Instant.parse("2026-07-28T12:30:00Z")
        );

        EmailNotification notification = service.criticalFeedback(
                feedback,
                "corr-1",
                Instant.parse("2026-07-28T12:31:00Z")
        );

        assertEquals(NotificationType.CRITICAL_FEEDBACK, notification.type());
        assertTrue(notification.plainTextBody().contains("Descricao: Erro <critico>"));
        assertTrue(notification.plainTextBody().contains("Urgencia: CRITICA"));
        assertTrue(notification.plainTextBody().contains("Data de envio:"));
        assertTrue(notification.htmlBody().contains("Erro &lt;critico&gt;"));
        assertFalse(notification.htmlBody().contains("Erro <critico>"));
    }
}
