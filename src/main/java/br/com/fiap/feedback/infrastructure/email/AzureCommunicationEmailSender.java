package br.com.fiap.feedback.infrastructure.email;

import br.com.fiap.feedback.domain.EmailNotification;
import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.util.polling.SyncPoller;

public final class AzureCommunicationEmailSender implements EmailSender {
    private final EmailClient emailClient;
    private final String senderAddress;
    private final String adminEmail;

    public AzureCommunicationEmailSender(String connectionString, String senderAddress, String adminEmail) {
        this.emailClient = new EmailClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        this.senderAddress = senderAddress;
        this.adminEmail = adminEmail;
    }

    @Override
    public String send(EmailNotification notification) {
        EmailMessage message = new EmailMessage()
                .setSenderAddress(senderAddress)
                .setToRecipients(adminEmail)
                .setSubject(notification.subject())
                .setBodyPlainText(notification.plainTextBody())
                .setBodyHtml(notification.htmlBody());

        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message, null);
        EmailSendResult result = poller.getFinalResult();

        if (result.getStatus() != EmailSendStatus.SUCCEEDED) {
            String reason = result.getError() == null ? "Erro sem detalhes." : result.getError().getMessage();
            throw new IllegalStateException("Falha no envio do e-mail: " + reason);
        }
        return result.getId();
    }
}
