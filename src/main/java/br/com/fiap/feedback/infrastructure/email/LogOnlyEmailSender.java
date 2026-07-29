package br.com.fiap.feedback.infrastructure.email;

import br.com.fiap.feedback.domain.EmailNotification;

import java.util.UUID;
import java.util.logging.Logger;

public final class LogOnlyEmailSender implements EmailSender {
    private final Logger logger;

    public LogOnlyEmailSender(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String send(EmailNotification notification) {
        String operationId = "log-" + UUID.randomUUID();
        logger.info("emailProvider=log operationId=" + operationId
                + " type=" + notification.type()
                + " subject=\"" + notification.subject().replace('\n', ' ') + "\"");
        return operationId;
    }
}
