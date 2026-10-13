package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.payloads.RequestReturnedPayload;
import dev.enzosantos.docbox.domain.requests.EvaluatedRequest;
import dev.enzosantos.docbox.domain.requests.Request;
import dev.enzosantos.docbox.domain.requests.ReturnedRequest;
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

  public void execute(UUID id, UUID userId)
      throws RequestNotFoundException, InvalidRequestTransitionException {
    final Optional<Request> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final Request request = requestResult.get();
    final boolean isStrictlyEvaluated =
        request instanceof EvaluatedRequest
            && !(request instanceof ReturnedRequest);
    if (!isStrictlyEvaluated) {
      throw new InvalidRequestTransitionException();
    }

    final Instant returnedAt = timeProvider.provideInstant();
    requestRepository.markAsReturned(
        id,
        new RequestReturnedPayload() {

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
