package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestReturnedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractEvaluatedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractReturnedRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class ReturnRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public ReturnRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId) throws InvalidRequestTransitionException {
    final Optional<AbstractRequest> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new InvalidRequestTransitionException();
    }

    final AbstractRequest request = requestResult.get();
    final boolean isStrictlyEvaluated =
        request instanceof AbstractEvaluatedRequest
            && !(request instanceof AbstractReturnedRequest);
    if (!isStrictlyEvaluated) {
      throw new InvalidRequestTransitionException();
    }

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
