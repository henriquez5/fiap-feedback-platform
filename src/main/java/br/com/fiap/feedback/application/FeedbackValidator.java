package br.com.fiap.feedback.application;

import br.com.fiap.feedback.domain.FeedbackRequest;

import java.util.ArrayList;
import java.util.List;

public final class FeedbackValidator {
    public static final int MAX_DESCRIPTION_LENGTH = 1000;

    public void validate(FeedbackRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            throw new ValidationException(List.of("O corpo da requisicao e obrigatorio."));
        }

        if (request.descricao() == null || request.descricao().isBlank()) {
            errors.add("descricao: campo obrigatorio.");
        } else if (request.descricao().trim().length() > MAX_DESCRIPTION_LENGTH) {
            errors.add("descricao: deve possuir no maximo " + MAX_DESCRIPTION_LENGTH + " caracteres.");
        }

        if (request.nota() == null) {
            errors.add("nota: campo obrigatorio.");
        } else if (request.nota() < 0 || request.nota() > 10) {
            errors.add("nota: deve estar entre 0 e 10.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
