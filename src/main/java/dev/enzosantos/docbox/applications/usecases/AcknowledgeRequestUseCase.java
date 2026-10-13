package dev.enzosantos.docbox.applications.usecases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.requests.AbstractRequestAcknowledgedPayload;
import java.time.Instant;
import java.util.UUID;

public class AcknowledgeRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public AcknowledgeRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId) {
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
