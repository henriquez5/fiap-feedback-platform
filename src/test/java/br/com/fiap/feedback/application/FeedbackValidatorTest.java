package br.com.fiap.feedback.application;

import br.com.fiap.feedback.domain.FeedbackRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FeedbackValidatorTest {
    private final FeedbackValidator validator = new FeedbackValidator();

    @Test
    void shouldAcceptValidRequest() {
        assertDoesNotThrow(() -> validator.validate(new FeedbackRequest("Aula excelente", 10)));
    }

    @Test
    void shouldRejectEmptyDescriptionAndInvalidScore() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(new FeedbackRequest("  ", 11))
        );
        assertEquals(2, exception.details().size());
    }

    @Test
    void shouldRejectNullRequest() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(null)
        );
        assertEquals(1, exception.details().size());
    }
}
