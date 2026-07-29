package br.com.fiap.feedback.application;

import br.com.fiap.feedback.domain.Urgency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrgencyClassifierTest {
    private final UrgencyClassifier classifier = new UrgencyClassifier();

    @Test
    void shouldClassifyCriticalScores() {
        assertEquals(Urgency.CRITICA, classifier.classify(0));
        assertEquals(Urgency.CRITICA, classifier.classify(3));
    }

    @Test
    void shouldClassifyAttentionScores() {
        assertEquals(Urgency.ATENCAO, classifier.classify(4));
        assertEquals(Urgency.ATENCAO, classifier.classify(6));
    }

    @Test
    void shouldClassifyNormalScores() {
        assertEquals(Urgency.NORMAL, classifier.classify(7));
        assertEquals(Urgency.NORMAL, classifier.classify(10));
    }
}
