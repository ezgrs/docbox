package dev.enzosantos.docbox.applications.usecases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestEvaluatedPayload;
import java.time.Instant;
import java.util.UUID;

public class AcceptRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public AcceptRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId) {
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
