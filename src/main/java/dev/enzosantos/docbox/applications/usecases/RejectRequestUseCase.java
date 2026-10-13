package dev.enzosantos.docbox.applications.usecases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestRejectedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractAcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractEvaluatedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class RejectRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public RejectRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId, String reason)
      throws InvalidRequestTransitionException {
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

    final Instant rejectedAt = timeProvider.provideInstant();
    requestRepository.markAsRejected(
        id,
        new AbstractRequestRejectedPayload() {

          @Override
          public Instant getEvaluatedAt() {
            return rejectedAt;
          }

          @Override
          public UUID getEvaluatorId() {
            return userId;
          }

          @Override
          public String getRejectionReason() {
            return reason;
          }
        });
  }
}
