package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.payloads.RequestReceivedPayload;
import dev.enzosantos.docbox.domain.requests.ReceivedRequest;
import dev.enzosantos.docbox.domain.requests.Request;
import dev.enzosantos.docbox.domain.requests.ReturnedRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class ReceiveRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public ReceiveRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
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
    final boolean isStrictlyReturned =
        request instanceof ReturnedRequest && !(request instanceof ReceivedRequest);
    if (!isStrictlyReturned) {
      throw new InvalidRequestTransitionException();
    }

    final Instant receivedAt = timeProvider.provideInstant();
    requestRepository.markAsReceived(
        id,
        new RequestReceivedPayload() {

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
