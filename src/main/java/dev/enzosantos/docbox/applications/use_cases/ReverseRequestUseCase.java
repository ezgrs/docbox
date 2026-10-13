package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.UnauthorizedRequestException;
import dev.enzosantos.docbox.domain.payloads.RequestEvaluatedPayload;
import dev.enzosantos.docbox.domain.payloads.RequestRejectedPayload;
import dev.enzosantos.docbox.domain.request_evaluations.RequestApproval;
import dev.enzosantos.docbox.domain.request_evaluations.RequestEvaluation;
import dev.enzosantos.docbox.domain.request_evaluations.RequestRejection;
import dev.enzosantos.docbox.domain.requests.AcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.ReceivedRequest;
import dev.enzosantos.docbox.domain.requests.Request;
import dev.enzosantos.docbox.domain.requests.ReturnedRequest;
import dev.enzosantos.docbox.domain.requests.SentRequest;
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
    final Optional<Request> requestResult = requestRepository.read(id);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final Request request = requestResult.get();
    if (request instanceof SentRequest sentRequest
        && !(request instanceof AcknowledgedRequest)) {
      if (!userId.equals(sentRequest.getSenderId())) {
        throw new UnauthorizedRequestException();
      }
      requestRepository.markAsNew(id);
      return;
    }
    if (request instanceof ReturnedRequest returnedRequest
        && !(request instanceof ReceivedRequest)) {
      if (!userId.equals(returnedRequest.getReturnerId())) {
        throw new UnauthorizedRequestException();
      }
      final RequestEvaluation evaluation = returnedRequest.getEvaluation();
      if (evaluation instanceof RequestApproval) {
        requestRepository.markAsAccepted(
            id,
            new RequestEvaluatedPayload() {

              @Override
              public Instant getEvaluatedAt() {
                return returnedRequest.getEvaluatedAt();
              }

              @Override
              public UUID getEvaluatorId() {
                return returnedRequest.getEvaluatorId();
              }
            });
      } else if (evaluation instanceof RequestRejection rejectionEvaluation) {
        requestRepository.markAsRejected(
            id,
            new RequestRejectedPayload() {

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
