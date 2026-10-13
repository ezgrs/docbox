package dev.enzosantos.docbox.applications.usecases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestCompletedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractCompletedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractReceivedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class CompleteRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public CompleteRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId) throws InvalidRequestTransitionException {
    final Optional<AbstractRequest> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new InvalidRequestTransitionException();
    }

    final AbstractRequest request = requestResult.get();
    final boolean isStrictlyReceived =
        request instanceof AbstractReceivedRequest && !(request instanceof AbstractCompletedRequest);
    if (!isStrictlyReceived) {
      throw new InvalidRequestTransitionException();
    }

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
