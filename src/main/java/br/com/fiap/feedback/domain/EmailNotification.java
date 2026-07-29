package br.com.fiap.feedback.domain;

import java.time.Instant;

public record EmailNotification(
        String id,
        NotificationType type,
        String subject,
        String plainTextBody,
        String htmlBody,
        String correlationId,
        Instant createdAt
) {
}
