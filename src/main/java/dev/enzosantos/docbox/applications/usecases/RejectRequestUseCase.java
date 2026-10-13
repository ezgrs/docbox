package dev.enzosantos.docbox.applications.usecases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.requests.AbstractRequestRejectedPayload;
import java.time.Instant;
import java.util.UUID;

public class RejectRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public RejectRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId, String reason) {
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
