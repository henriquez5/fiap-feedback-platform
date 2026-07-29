package br.com.fiap.feedback.application;

import br.com.fiap.feedback.domain.Urgency;

public final class UrgencyClassifier {
    public Urgency classify(int score) {
        if (score <= 3) {
            return Urgency.CRITICA;
        }
        if (score <= 6) {
            return Urgency.ATENCAO;
        }
        return Urgency.NORMAL;
    }
}
