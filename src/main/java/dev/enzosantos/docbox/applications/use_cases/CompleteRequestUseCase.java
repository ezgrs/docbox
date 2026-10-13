package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.payloads.RequestCompletedPayload;
import dev.enzosantos.docbox.domain.requests.CompletedRequest;
import dev.enzosantos.docbox.domain.requests.ReceivedRequest;
import dev.enzosantos.docbox.domain.requests.Request;
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

  public void execute(UUID id, UUID userId)
      throws RequestNotFoundException, InvalidRequestTransitionException {
    final Optional<Request> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final Request request = requestResult.get();
    final boolean isStrictlyReceived =
        request instanceof ReceivedRequest
            && !(request instanceof CompletedRequest);
    if (!isStrictlyReceived) {
      throw new InvalidRequestTransitionException();
    }

    final Instant completedAt = timeProvider.provideInstant();
    requestRepository.markAsCompleted(
        id,
        new RequestCompletedPayload() {

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
