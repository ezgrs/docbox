package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.UnauthorizedRequestException;
import dev.enzosantos.docbox.domain.payloads.RequestUpdatedPayload;
import dev.enzosantos.docbox.domain.requests.Request;
import dev.enzosantos.docbox.domain.requests.SentRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class UpdateRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public UpdateRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, String name, Optional<String> description, UUID userId)
      throws RequestNotFoundException,
          InvalidRequestTransitionException,
          UnauthorizedRequestException {
    final Optional<Request> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final Request request = requestResult.get();
    if (request instanceof SentRequest) {
      throw new InvalidRequestTransitionException();
    }

    if (!userId.equals(request.getCreatorId())) {
      throw new UnauthorizedRequestException();
    }

    final Instant updatedAt = timeProvider.provideInstant();
    requestRepository.update(
        id,
        new RequestUpdatedPayload() {
          @Override
          public String getName() {
            return name;
          }

          @Override
          public Optional<String> getDescription() {
            return description;
          }

          @Override
          public Instant getUpdatedAt() {
            return updatedAt;
          }
        });
  }
}
