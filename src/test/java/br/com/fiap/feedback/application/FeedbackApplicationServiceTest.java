package br.com.fiap.feedback.application;

import br.com.fiap.feedback.domain.Feedback;
import br.com.fiap.feedback.domain.FeedbackRequest;
import br.com.fiap.feedback.domain.Urgency;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeedbackApplicationServiceTest {
    @Test
    void shouldCreateNormalizedCriticalFeedback() {
        Instant now = Instant.parse("2026-07-28T20:00:00Z");
        FeedbackApplicationService service = new FeedbackApplicationService(
                new FeedbackValidator(),
                new UrgencyClassifier(),
                Clock.fixed(now, ZoneOffset.UTC),
                () -> "feedback-1"
        );

        Feedback result = service.create(new FeedbackRequest("  Laboratorio com erro  ", 2));

        assertEquals("feedback-1", result.id());
        assertEquals(Feedback.PARTITION_KEY, result.partitionKey());
        assertEquals("Laboratorio com erro", result.descricao());
        assertEquals(2, result.nota());
        assertEquals(Urgency.CRITICA, result.urgencia());
        assertEquals(now, result.dataEnvio());
    }
}
