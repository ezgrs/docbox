package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.payloads.RequestRejectedPayload;
import dev.enzosantos.docbox.domain.requests.AcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.EvaluatedRequest;
import dev.enzosantos.docbox.domain.requests.Request;
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
      throws RequestNotFoundException, InvalidRequestTransitionException {
    final Optional<Request> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final Request request = requestResult.get();
    final boolean isStrictlyAcknowledged =
        request instanceof AcknowledgedRequest
            && !(request instanceof EvaluatedRequest);
    if (!isStrictlyAcknowledged) {
      throw new InvalidRequestTransitionException();
    }

    final Instant rejectedAt = timeProvider.provideInstant();
    requestRepository.markAsRejected(
        id,
        new RequestRejectedPayload() {

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
