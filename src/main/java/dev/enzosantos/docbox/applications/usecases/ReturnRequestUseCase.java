package dev.enzosantos.docbox.applications.usecases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.requests.AbstractRequestReturnedPayload;
import java.time.Instant;
import java.util.UUID;

public class ReturnRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public ReturnRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId) {
    final Instant returnedAt = timeProvider.provideInstant();
    requestRepository.markAsReturned(
        id,
        new AbstractRequestReturnedPayload() {

          @Override
          public Instant getReturnedAt() {
            return returnedAt;
          }

          @Override
          public UUID getReturnerId() {
            return userId;
          }
        });
  }
}
