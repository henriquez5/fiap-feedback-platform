package br.com.fiap.feedback.domain;

import java.time.Instant;

public record FeedbackResponse(
        String id,
        String descricao,
        int nota,
        Urgency urgencia,
        Instant dataEnvio
) {
    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(
                feedback.id(),
                feedback.descricao(),
                feedback.nota(),
                feedback.urgencia(),
                feedback.dataEnvio()
        );
    }
}
