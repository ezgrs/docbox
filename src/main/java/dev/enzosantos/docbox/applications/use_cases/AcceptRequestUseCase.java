package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestEvaluatedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractAcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractEvaluatedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class AcceptRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public AcceptRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId) throws InvalidRequestTransitionException {
    final Optional<AbstractRequest> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new InvalidRequestTransitionException();
    }

    final AbstractRequest request = requestResult.get();
    final boolean isStrictlyAcknowledged =
        request instanceof AbstractAcknowledgedRequest
            && !(request instanceof AbstractEvaluatedRequest);
    if (!isStrictlyAcknowledged) {
      throw new InvalidRequestTransitionException();
    }

    final Instant acceptedAt = timeProvider.provideInstant();
    requestRepository.markAsAccepted(
        id,
        new AbstractRequestEvaluatedPayload() {

          @Override
          public Instant getEvaluatedAt() {
            return acceptedAt;
          }

          @Override
          public UUID getEvaluatorId() {
            return userId;
          }
        });
  }
}
