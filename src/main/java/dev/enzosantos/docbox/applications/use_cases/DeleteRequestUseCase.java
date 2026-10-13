package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.UnauthorizedRequestException;
import dev.enzosantos.docbox.domain.requests.Request;
import dev.enzosantos.docbox.domain.requests.SentRequest;
import java.util.Optional;
import java.util.UUID;

public class DeleteRequestUseCase {
  private final RequestRepository requestRepository;

  public DeleteRequestUseCase(RequestRepository requestRepository) {
    this.requestRepository = requestRepository;
  }

  public void execute(UUID id, UUID userId)
      throws RequestNotFoundException,
          UnauthorizedRequestException,
          InvalidRequestTransitionException {
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

    requestRepository.delete(id);
  }
}
