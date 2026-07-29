package br.com.fiap.feedback.infrastructure;

import com.microsoft.azure.functions.ExecutionContext;

import java.util.logging.Level;

public final class StructuredLog {
    private StructuredLog() {
    }

    public static void info(ExecutionContext context, String event, String correlationId, String details) {
        context.getLogger().info(format(event, correlationId, details));
    }

    public static void warning(ExecutionContext context, String event, String correlationId, String details) {
        context.getLogger().warning(format(event, correlationId, details));
    }

    public static void error(ExecutionContext context, String event, String correlationId, Throwable error) {
        context.getLogger().log(Level.SEVERE,
                format(event, correlationId, error == null ? "unknown" : error.getMessage()), error);
    }

    private static String format(String event, String correlationId, String details) {
        String safeDetails = details == null ? "" : details.replace('\n', ' ').replace('\r', ' ');
        return "event=" + event + " correlationId=" + correlationId + " details=\"" + safeDetails + "\"";
    }
}
