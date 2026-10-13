package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestReceivedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractReceivedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractReturnedRequest;
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
    final Optional<AbstractRequest> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final AbstractRequest request = requestResult.get();
    final boolean isStrictlyReturned =
        request instanceof AbstractReturnedRequest && !(request instanceof AbstractReceivedRequest);
    if (!isStrictlyReturned) {
      throw new InvalidRequestTransitionException();
    }

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
