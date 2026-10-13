package dev.enzosantos.docbox.applications.usecases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.requests.AbstractRequestCompletedPayload;
import java.time.Instant;
import java.util.UUID;

public class CompleteRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public CompleteRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId) {
    final Instant completedAt = timeProvider.provideInstant();
    requestRepository.markAsCompleted(
        id,
        new AbstractRequestCompletedPayload() {

          @Override
          public Instant getCompletedAt() {
            return completedAt;
          }

          @Override
          public UUID getCompleterId() {
            return userId;
          }
        });
  }
}
