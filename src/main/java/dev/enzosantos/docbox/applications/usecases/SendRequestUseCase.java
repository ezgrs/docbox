package dev.enzosantos.docbox.applications.usecases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestSentPayload;
import java.time.Instant;
import java.util.UUID;

public class SendRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public SendRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId) {
    final Instant sentAt = timeProvider.provideInstant();
    requestRepository.markAsSent(
        id,
        new AbstractRequestSentPayload() {
          @Override
          public Instant getSentAt() {
            return sentAt;
          }

          @Override
          public UUID getSenderId() {
            return userId;
          }
        });
  }
}
