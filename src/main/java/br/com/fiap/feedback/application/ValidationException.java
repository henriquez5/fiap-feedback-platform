package br.com.fiap.feedback.application;

import java.util.List;

public final class ValidationException extends RuntimeException {
    private final List<String> details;

    public ValidationException(List<String> details) {
        super("Dados de entrada invalidos.");
        this.details = List.copyOf(details);
    }

    public List<String> details() {
        return details;
    }
}
