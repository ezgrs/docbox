package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.payloads.RequestSentPayload;
import dev.enzosantos.docbox.domain.request_evaluations.RequestRejection;
import dev.enzosantos.docbox.domain.requests.ReceivedRequest;
import dev.enzosantos.docbox.domain.requests.Request;
import dev.enzosantos.docbox.domain.requests.SentRequest;
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

  public void execute(UUID id, UUID userId)
      throws RequestNotFoundException, InvalidRequestTransitionException {
    final Optional<Request> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final Request request = requestResult.get();
    final boolean isRejectedReceivedRequest =
        request instanceof ReceivedRequest receivedRequest
            && receivedRequest.getEvaluation() instanceof RequestRejection;
    final boolean isInitialRequest = !(request instanceof SentRequest);

    if (!isInitialRequest && !isRejectedReceivedRequest) {
      throw new InvalidRequestTransitionException();
    }

    final Instant sentAt = timeProvider.provideInstant();
    requestRepository.markAsSent(
        id,
        new RequestSentPayload() {
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
