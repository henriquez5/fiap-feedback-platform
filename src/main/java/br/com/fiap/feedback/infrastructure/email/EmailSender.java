package br.com.fiap.feedback.infrastructure.email;

import br.com.fiap.feedback.domain.EmailNotification;

public interface EmailSender {
    String send(EmailNotification notification);
}
