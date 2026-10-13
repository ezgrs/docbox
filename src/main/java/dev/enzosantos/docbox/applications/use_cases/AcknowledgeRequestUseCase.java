package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestAcknowledgedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractAcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractSentRequest;
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

  public void execute(UUID id, UUID userId) throws InvalidRequestTransitionException {
    final Optional<AbstractRequest> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new InvalidRequestTransitionException();
    }

    final AbstractRequest request = requestResult.get();
    if (request instanceof AbstractAcknowledgedRequest) {
      throw new InvalidRequestTransitionException();
    }
    if (!(request instanceof AbstractSentRequest)) {
      throw new InvalidRequestTransitionException();
    }

    final Instant acknowledgedAt = timeProvider.provideInstant();
    requestRepository.markAsAcknowledged(
        id,
        new AbstractRequestAcknowledgedPayload() {

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
