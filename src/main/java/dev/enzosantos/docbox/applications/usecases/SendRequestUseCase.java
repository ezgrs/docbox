package dev.enzosantos.docbox.applications.usecases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestSentPayload;
import dev.enzosantos.docbox.domain.request_evaluations.AbstractRequestRejection;
import dev.enzosantos.docbox.domain.requests.AbstractReceivedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractSentRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class SendRequestUseCase {
  private final RequestRepository requestRepository;
  private final TimeProvider timeProvider;

  public SendRequestUseCase(RequestRepository requestRepository, TimeProvider timeProvider) {
    this.requestRepository = requestRepository;
    this.timeProvider = timeProvider;
  }

  public void execute(UUID id, UUID userId) throws InvalidRequestTransitionException {
    final Optional<AbstractRequest> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new InvalidRequestTransitionException();
    }

    final AbstractRequest request = requestResult.get();
    final boolean isRejectedReceivedRequest =
        request instanceof AbstractReceivedRequest receivedRequest
            && receivedRequest.getEvaluation() instanceof AbstractRequestRejection;
    final boolean isInitialRequest = !(request instanceof AbstractSentRequest);

    if (!isInitialRequest && !isRejectedReceivedRequest) {
      throw new InvalidRequestTransitionException();
    }

    final Instant sentAt = timeProvider.provideInstant();
    requestRepository.markAsSent(
        id,
        new AbstractRequestSentPayload() {
          @Override
          public Instant getSentAt() {
            return sentAt;
          }

          @Override
          public UUID getSenderId() {
            return userId;
          }
        });
  }
}
