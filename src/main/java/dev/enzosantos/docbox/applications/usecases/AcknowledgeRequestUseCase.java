package dev.enzosantos.docbox.applications.usecases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestAcknowledgedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractAcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import java.time.Instant;
import java.util.UUID;

public class AcknowledgeRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public AcknowledgeRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId) throws InvalidRequestTransitionException {
    final AbstractRequest request = requestRepository.read(id);
    if (request instanceof AbstractAcknowledgedRequest) {
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
