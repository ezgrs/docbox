package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractSentRequest;
import java.util.Optional;
import java.util.UUID;

public class DeleteRequestUseCase {
  private final RequestRepository requestRepository;

  public DeleteRequestUseCase(RequestRepository requestRepository) {
    this.requestRepository = requestRepository;
  }

  public void execute(UUID id) throws RequestNotFoundException, InvalidRequestTransitionException {
    final Optional<AbstractRequest> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final AbstractRequest request = requestResult.get();
    if (request instanceof AbstractSentRequest) {
      throw new InvalidRequestTransitionException();
    }

    requestRepository.delete(id);
  }
}
