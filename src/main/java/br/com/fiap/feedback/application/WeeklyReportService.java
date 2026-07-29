package br.com.fiap.feedback.application;

import br.com.fiap.feedback.domain.Feedback;
import br.com.fiap.feedback.domain.Urgency;
import br.com.fiap.feedback.domain.WeeklyReport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class WeeklyReportService {
    public WeeklyReport generate(List<Feedback> source, Instant periodEnd) {
        Instant periodStart = periodEnd.minus(7, ChronoUnit.DAYS);

        List<Feedback> selected = source == null
                ? new ArrayList<>()
                : source.stream()
                        .filter(item -> item != null && item.dataEnvio() != null)
                        .filter(item -> !item.dataEnvio().isBefore(periodStart))
                        .filter(item -> item.dataEnvio().isBefore(periodEnd))
                        .sorted(Comparator.comparing(Feedback::dataEnvio))
                        .toList();

        Map<LocalDate, Long> quantityByDay = new TreeMap<>();
        Map<Urgency, Long> quantityByUrgency = new EnumMap<>(Urgency.class);
        for (Urgency urgency : Urgency.values()) {
            quantityByUrgency.put(urgency, 0L);
        }

        long scoreSum = 0;
        for (Feedback feedback : selected) {
            LocalDate date = feedback.dataEnvio().atZone(ZoneOffset.UTC).toLocalDate();
            quantityByDay.merge(date, 1L, Long::sum);
            quantityByUrgency.merge(feedback.urgencia(), 1L, Long::sum);
            scoreSum += feedback.nota();
        }

        double average = selected.isEmpty()
                ? 0.0
                : BigDecimal.valueOf((double) scoreSum / selected.size())
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();

        return new WeeklyReport(
                periodStart,
                periodEnd,
                selected.size(),
                average,
                Map.copyOf(quantityByDay),
                Map.copyOf(quantityByUrgency),
                List.copyOf(selected)
        );
    }
}
