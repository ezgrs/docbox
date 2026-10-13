package dev.enzosantos.docbox.applications.usecases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.requests.AbstractRequestReceivedPayload;
import java.time.Instant;
import java.util.UUID;

public class ReceiveRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public ReceiveRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId) {
    final Instant receivedAt = timeProvider.provideInstant();
    requestRepository.markAsReceived(
        id,
        new AbstractRequestReceivedPayload() {

          @Override
          public Instant getReceivedAt() {
            return receivedAt;
          }

          @Override
          public UUID getReceiverId() {
            return userId;
          }
        });
  }
}
