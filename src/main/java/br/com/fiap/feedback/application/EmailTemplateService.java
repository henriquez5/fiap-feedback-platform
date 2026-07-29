package br.com.fiap.feedback.application;

import br.com.fiap.feedback.domain.EmailNotification;
import br.com.fiap.feedback.domain.Feedback;
import br.com.fiap.feedback.domain.NotificationType;
import br.com.fiap.feedback.domain.Urgency;
import br.com.fiap.feedback.domain.WeeklyReport;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class EmailTemplateService {
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC);

    public EmailNotification criticalFeedback(Feedback feedback, String correlationId, Instant createdAt) {
        String subject = "[URGENTE] Feedback critico recebido";
        String plainText = """
                Um feedback critico foi recebido.

                Descricao: %s
                Urgencia: %s
                Data de envio: %s
                Nota: %d
                Identificador: %s
                """.formatted(
                feedback.descricao(),
                feedback.urgencia(),
                DATE_TIME.format(feedback.dataEnvio()),
                feedback.nota(),
                feedback.id()
        );

        String html = """
                <h2 style="color:#b91c1c">Feedback critico recebido</h2>
                <table style="border-collapse:collapse">
                  <tr><th style="text-align:left;padding:6px">Descricao</th><td style="padding:6px">%s</td></tr>
                  <tr><th style="text-align:left;padding:6px">Urgencia</th><td style="padding:6px"><strong>%s</strong></td></tr>
                  <tr><th style="text-align:left;padding:6px">Data de envio</th><td style="padding:6px">%s</td></tr>
                  <tr><th style="text-align:left;padding:6px">Nota</th><td style="padding:6px">%d</td></tr>
                  <tr><th style="text-align:left;padding:6px">Identificador</th><td style="padding:6px">%s</td></tr>
                </table>
                """.formatted(
                escapeHtml(feedback.descricao()),
                feedback.urgencia(),
                DATE_TIME.format(feedback.dataEnvio()),
                feedback.nota(),
                feedback.id()
        );

        return new EmailNotification(
                UUID.randomUUID().toString(),
                NotificationType.CRITICAL_FEEDBACK,
                subject,
                plainText,
                html,
                correlationId,
                createdAt
        );
    }

    public EmailNotification weeklyReport(WeeklyReport report, String correlationId, Instant createdAt) {
        StringBuilder plain = new StringBuilder();
        plain.append("Relatorio semanal de feedbacks\n\n")
                .append("Periodo: ").append(DATE_TIME.format(report.periodStart()))
                .append(" ate ").append(DATE_TIME.format(report.periodEnd())).append('\n')
                .append("Total de avaliacoes: ").append(report.totalFeedbacks()).append('\n')
                .append("Media das notas: ").append(String.format("%.2f", report.averageScore())).append("\n\n")
                .append("Quantidade por dia:\n");
        report.quantityByDay().forEach((day, count) ->
                plain.append("- ").append(day).append(": ").append(count).append('\n'));
        plain.append("\nQuantidade por urgencia:\n");
        for (Urgency urgency : Urgency.values()) {
            plain.append("- ").append(urgency).append(": ")
                    .append(report.quantityByUrgency().getOrDefault(urgency, 0L)).append('\n');
        }
        plain.append("\nFeedbacks do periodo:\n");
        if (report.feedbacks().isEmpty()) {
            plain.append("Nenhum feedback recebido no periodo.\n");
        } else {
            report.feedbacks().forEach(feedback -> plain.append("- ")
                    .append(DATE_TIME.format(feedback.dataEnvio())).append(" | ")
                    .append(feedback.urgencia()).append(" | nota ")
                    .append(feedback.nota()).append(" | ")
                    .append(feedback.descricao()).append('\n'));
        }

        StringBuilder html = new StringBuilder();
        html.append("<h2>Relatorio semanal de feedbacks</h2>")
                .append("<p><strong>Periodo:</strong> ")
                .append(DATE_TIME.format(report.periodStart())).append(" ate ")
                .append(DATE_TIME.format(report.periodEnd())).append("</p>")
                .append("<p><strong>Total:</strong> ").append(report.totalFeedbacks())
                .append(" &nbsp; <strong>Media:</strong> ")
                .append(String.format("%.2f", report.averageScore())).append("</p>")
                .append("<h3>Quantidade por dia</h3><ul>");
        report.quantityByDay().forEach((day, count) -> html.append("<li>")
                .append(day).append(": ").append(count).append("</li>"));
        html.append("</ul><h3>Quantidade por urgencia</h3><ul>");
        for (Urgency urgency : Urgency.values()) {
            html.append("<li>").append(urgency).append(": ")
                    .append(report.quantityByUrgency().getOrDefault(urgency, 0L)).append("</li>");
        }
        html.append("</ul><h3>Detalhamento</h3>")
                .append("<table style=\"border-collapse:collapse;width:100%\">")
                .append("<thead><tr><th style=\"border:1px solid #ddd;padding:6px\">Data</th>")
                .append("<th style=\"border:1px solid #ddd;padding:6px\">Urgencia</th>")
                .append("<th style=\"border:1px solid #ddd;padding:6px\">Nota</th>")
                .append("<th style=\"border:1px solid #ddd;padding:6px\">Descricao</th></tr></thead><tbody>");
        if (report.feedbacks().isEmpty()) {
            html.append("<tr><td colspan=\"4\" style=\"border:1px solid #ddd;padding:6px\">Nenhum feedback recebido.</td></tr>");
        } else {
            for (Feedback feedback : report.feedbacks()) {
                html.append("<tr><td style=\"border:1px solid #ddd;padding:6px\">")
                        .append(DATE_TIME.format(feedback.dataEnvio())).append("</td><td style=\"border:1px solid #ddd;padding:6px\">")
                        .append(feedback.urgencia()).append("</td><td style=\"border:1px solid #ddd;padding:6px\">")
                        .append(feedback.nota()).append("</td><td style=\"border:1px solid #ddd;padding:6px\">")
                        .append(escapeHtml(feedback.descricao())).append("</td></tr>");
            }
        }
        html.append("</tbody></table>");

        return new EmailNotification(
                UUID.randomUUID().toString(),
                NotificationType.WEEKLY_REPORT,
                "Relatorio semanal de feedbacks",
                plain.toString(),
                html.toString(),
                correlationId,
                createdAt
        );
    }

    static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
