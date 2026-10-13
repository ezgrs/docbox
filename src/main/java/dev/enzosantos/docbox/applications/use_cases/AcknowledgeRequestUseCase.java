package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.payloads.RequestAcknowledgedPayload;
import dev.enzosantos.docbox.domain.requests.AcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.Request;
import dev.enzosantos.docbox.domain.requests.SentRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class AcknowledgeRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public AcknowledgeRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId)
      throws RequestNotFoundException, InvalidRequestTransitionException {
    final Optional<Request> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final Request request = requestResult.get();
    if (request instanceof AcknowledgedRequest) {
      throw new InvalidRequestTransitionException();
    }
    if (!(request instanceof SentRequest)) {
      throw new InvalidRequestTransitionException();
    }

    final Instant acknowledgedAt = timeProvider.provideInstant();
    requestRepository.markAsAcknowledged(
        id,
        new RequestAcknowledgedPayload() {

          @Override
          public Instant getAcknowledgedAt() {
            return acknowledgedAt;
          }

          @Override
          public UUID getAcknowledgerId() {
            return userId;
          }
        });
  }
}
