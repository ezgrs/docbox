package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.UnauthorizedRequestException;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestUpdatedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractSentRequest;
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

  public void execute(UUID id, String name, Optional<String> description, String userId)
      throws InvalidRequestTransitionException, UnauthorizedRequestException {
    final Optional<AbstractRequest> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new InvalidRequestTransitionException();
    }

    final AbstractRequest request = requestResult.get();
    if (request instanceof AbstractSentRequest) {
      throw new InvalidRequestTransitionException();
    }

    if (!userId.equals(request.getCreatorId())) {
      throw new UnauthorizedRequestException();
    }

    final Instant updatedAt = timeProvider.provideInstant();
    requestRepository.update(
        id,
        new AbstractRequestUpdatedPayload() {
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
