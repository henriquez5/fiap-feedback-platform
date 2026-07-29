package br.com.fiap.feedback.infrastructure.email;

import br.com.fiap.feedback.infrastructure.AppSettings;

import java.util.Locale;
import java.util.logging.Logger;

public final class EmailSenderFactory {
    private EmailSenderFactory() {
    }

    public static EmailSender create(Logger logger) {
        String provider = AppSettings.get("EMAIL_PROVIDER", "log").toLowerCase(Locale.ROOT);
        return switch (provider) {
            case "azure", "azure-communication-services", "acs" -> new AzureCommunicationEmailSender(
                    AppSettings.require("ACS_EMAIL_CONNECTION_STRING"),
                    AppSettings.require("ACS_EMAIL_SENDER"),
                    AppSettings.require("ADMIN_EMAIL")
            );
            case "log" -> new LogOnlyEmailSender(logger);
            default -> throw new IllegalStateException("EMAIL_PROVIDER nao suportado: " + provider);
        };
    }
}
