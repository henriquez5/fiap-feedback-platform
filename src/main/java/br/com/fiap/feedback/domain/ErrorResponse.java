package br.com.fiap.feedback.domain;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<String> details,
        String correlationId,
        Instant timestamp
) {
}
