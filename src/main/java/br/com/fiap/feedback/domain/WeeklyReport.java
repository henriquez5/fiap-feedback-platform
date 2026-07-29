package br.com.fiap.feedback.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record WeeklyReport(
        Instant periodStart,
        Instant periodEnd,
        long totalFeedbacks,
        double averageScore,
        Map<LocalDate, Long> quantityByDay,
        Map<Urgency, Long> quantityByUrgency,
        List<Feedback> feedbacks
) {
}
