package br.com.fiap.feedback.domain;

import java.time.Instant;

public record Feedback(
        String id,
        String partitionKey,
        String descricao,
        int nota,
        Urgency urgencia,
        Instant dataEnvio
) {
    public static final String PARTITION_KEY = "feedback";
}
