package br.com.fiap.feedback.application;

import br.com.fiap.feedback.domain.Feedback;
import br.com.fiap.feedback.domain.FeedbackRequest;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public final class FeedbackApplicationService {
    private final FeedbackValidator validator;
    private final UrgencyClassifier classifier;
    private final Clock clock;
    private final Supplier<String> idSupplier;

    public FeedbackApplicationService(
            FeedbackValidator validator,
            UrgencyClassifier classifier,
            Clock clock,
            Supplier<String> idSupplier
    ) {
        this.validator = Objects.requireNonNull(validator);
        this.classifier = Objects.requireNonNull(classifier);
        this.clock = Objects.requireNonNull(clock);
        this.idSupplier = Objects.requireNonNull(idSupplier);
    }

    public static FeedbackApplicationService production() {
        return new FeedbackApplicationService(
                new FeedbackValidator(),
                new UrgencyClassifier(),
                Clock.systemUTC(),
                () -> UUID.randomUUID().toString()
        );
    }

    public Feedback create(FeedbackRequest request) {
        validator.validate(request);
        Instant now = clock.instant();

        return new Feedback(
                idSupplier.get(),
                Feedback.PARTITION_KEY,
                request.descricao().trim(),
                request.nota(),
                classifier.classify(request.nota()),
                now
        );
    }
}
