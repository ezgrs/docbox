package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.UnauthorizedRequestException;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestEvaluatedPayload;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestRejectedPayload;
import dev.enzosantos.docbox.domain.request_evaluations.AbstractRequestApproval;
import dev.enzosantos.docbox.domain.request_evaluations.AbstractRequestEvaluation;
import dev.enzosantos.docbox.domain.request_evaluations.AbstractRequestRejection;
import dev.enzosantos.docbox.domain.requests.AbstractAcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractReceivedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractReturnedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractSentRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class ReverseRequestUseCase {
  private final RequestRepository requestRepository;

  public ReverseRequestUseCase(RequestRepository requestRepository) {
    this.requestRepository = requestRepository;
  }

  public void execute(UUID id, UUID userId)
      throws RequestNotFoundException,
          UnauthorizedRequestException,
          InvalidRequestTransitionException {
    final Optional<AbstractRequest> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final AbstractRequest request = requestResult.get();
    if (request instanceof AbstractSentRequest sentRequest
        && !(request instanceof AbstractAcknowledgedRequest)) {
      if (!userId.equals(sentRequest.getSenderId())) {
        throw new UnauthorizedRequestException();
      }
      requestRepository.markAsNew(id);
      return;
    }
    if (request instanceof AbstractReturnedRequest returnedRequest
        && !(request instanceof AbstractReceivedRequest)) {
      if (!userId.equals(returnedRequest.getReturnerId())) {
        throw new UnauthorizedRequestException();
      }
      final AbstractRequestEvaluation evaluation = returnedRequest.getEvaluation();
      if (evaluation instanceof AbstractRequestApproval) {
        requestRepository.markAsAccepted(
            id,
            new AbstractRequestEvaluatedPayload() {

              @Override
              public Instant getEvaluatedAt() {
                return returnedRequest.getEvaluatedAt();
              }

              @Override
              public UUID getEvaluatorId() {
                return returnedRequest.getEvaluatorId();
              }
            });
      } else if (evaluation instanceof AbstractRequestRejection rejectionEvaluation) {
        requestRepository.markAsRejected(
            id,
            new AbstractRequestRejectedPayload() {

              @Override
              public Instant getEvaluatedAt() {
                return returnedRequest.getEvaluatedAt();
              }

              @Override
              public UUID getEvaluatorId() {
                return returnedRequest.getEvaluatorId();
              }

              @Override
              public String getRejectionReason() {
                return rejectionEvaluation.getRejectionReason();
              }
            });
      } else {
        throw new InvalidRequestTransitionException();
      }
      return;
    }
    throw new InvalidRequestTransitionException();
  }
}
