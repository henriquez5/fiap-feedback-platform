package br.com.fiap.feedback.application;

import br.com.fiap.feedback.domain.Feedback;
import br.com.fiap.feedback.domain.Urgency;
import br.com.fiap.feedback.domain.WeeklyReport;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeeklyReportServiceTest {
    private final WeeklyReportService service = new WeeklyReportService();

    @Test
    void shouldAggregateOnlyLastSevenDays() {
        Instant end = Instant.parse("2026-07-28T12:00:00Z");
        List<Feedback> feedbacks = List.of(
                feedback("1", 10, Urgency.NORMAL, "2026-07-27T10:00:00Z"),
                feedback("2", 2, Urgency.CRITICA, "2026-07-27T11:00:00Z"),
                feedback("3", 5, Urgency.ATENCAO, "2026-07-25T11:00:00Z"),
                feedback("old", 0, Urgency.CRITICA, "2026-07-10T11:00:00Z")
        );

        WeeklyReport report = service.generate(feedbacks, end);

        assertEquals(3, report.totalFeedbacks());
        assertEquals(5.67, report.averageScore());
        assertEquals(2L, report.quantityByDay().get(Instant.parse("2026-07-27T00:00:00Z")
                .atZone(java.time.ZoneOffset.UTC).toLocalDate()));
        assertEquals(1L, report.quantityByUrgency().get(Urgency.CRITICA));
        assertEquals(1L, report.quantityByUrgency().get(Urgency.ATENCAO));
        assertEquals(1L, report.quantityByUrgency().get(Urgency.NORMAL));
    }

    private Feedback feedback(String id, int score, Urgency urgency, String instant) {
        return new Feedback(id, Feedback.PARTITION_KEY, "descricao " + id, score, urgency, Instant.parse(instant));
    }
}
