package dev.enzosantos.docbox.applications.use_cases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import dev.enzosantos.docbox.domain.requests.CompletedRequest;
import dev.enzosantos.docbox.domain.requests.ReceivedRequest;
import dev.enzosantos.docbox.domain.requests.ReturnedRequest;
import dev.enzosantos.docbox.domain.requests.SentRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReverseRequestUseCaseTest {

  @Mock private RequestRepository requestRepository;

  @Mock private SentRequest sentRequest;

  @Mock private AcknowledgedRequest acknowledgedRequest;

  @Mock private ReturnedRequest returnedRequest;

  @Mock private ReceivedRequest receivedRequest;

  @Mock private CompletedRequest completedRequest;

  @Mock private RequestEvaluation evaluation;

  @Mock private RequestApproval approval;

  @Mock private RequestRejection rejection;

  @InjectMocks private ReverseRequestUseCase useCase;

  @Test
  void shouldRejectReversingRequestThatDoesNotExist() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(RequestNotFoundException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verifyNoReverseOperation();
  }

  @Test
  void shouldMarkSentRequestAsNewForItsSender()
      throws RequestNotFoundException,
          UnauthorizedRequestException,
          InvalidRequestTransitionException {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(sentRequest));
    when(sentRequest.getSenderId()).thenReturn(userId);

    useCase.execute(requestId, userId);

    verify(requestRepository).read(requestId);
    verify(requestRepository).markAsNew(requestId);
    verify(requestRepository, never())
        .markAsAccepted(any(UUID.class), any(RequestEvaluatedPayload.class));
    verify(requestRepository, never())
        .markAsRejected(any(UUID.class), any(RequestRejectedPayload.class));
  }

  @Test
  void shouldRejectReversingSentRequestForAnotherUser() {
    final UUID requestId = UUID.randomUUID();
    final UUID senderId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(sentRequest));
    when(sentRequest.getSenderId()).thenReturn(senderId);

    assertThrows(UnauthorizedRequestException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verifyNoReverseOperation();
  }

  @Test
  void shouldRejectReversingAcknowledgedRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(acknowledgedRequest));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verifyNoReverseOperation();
  }

  @Test
  void shouldRestoreApprovedReturnedRequestForItsReturner()
      throws RequestNotFoundException,
          UnauthorizedRequestException,
          InvalidRequestTransitionException {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final UUID evaluatorId = UUID.randomUUID();
    final Instant evaluatedAt = Instant.parse("2026-10-13T12:00:00Z");

    when(requestRepository.read(requestId)).thenReturn(Optional.of(returnedRequest));
    when(returnedRequest.getReturnerId()).thenReturn(userId);
    when(returnedRequest.getEvaluation()).thenReturn(approval);
    when(returnedRequest.getEvaluatedAt()).thenReturn(evaluatedAt);
    when(returnedRequest.getEvaluatorId()).thenReturn(evaluatorId);

    useCase.execute(requestId, userId);

    final ArgumentCaptor<RequestEvaluatedPayload> payloadCaptor =
        ArgumentCaptor.forClass(RequestEvaluatedPayload.class);
    verify(requestRepository).read(requestId);
    verify(requestRepository).markAsAccepted(eq(requestId), payloadCaptor.capture());
    verify(requestRepository, never()).markAsNew(any(UUID.class));
    verify(requestRepository, never())
        .markAsRejected(any(UUID.class), any(RequestRejectedPayload.class));
    assertEquals(evaluatedAt, payloadCaptor.getValue().getEvaluatedAt());
    assertEquals(evaluatorId, payloadCaptor.getValue().getEvaluatorId());
  }

  @Test
  void shouldRestoreRejectedReturnedRequestForItsReturner()
      throws RequestNotFoundException,
          UnauthorizedRequestException,
          InvalidRequestTransitionException {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final UUID evaluatorId = UUID.randomUUID();
    final Instant evaluatedAt = Instant.parse("2026-10-13T12:00:00Z");
    final String reason = "Document is incomplete";

    when(requestRepository.read(requestId)).thenReturn(Optional.of(returnedRequest));
    when(returnedRequest.getReturnerId()).thenReturn(userId);
    when(returnedRequest.getEvaluation()).thenReturn(rejection);
    when(returnedRequest.getEvaluatedAt()).thenReturn(evaluatedAt);
    when(returnedRequest.getEvaluatorId()).thenReturn(evaluatorId);
    when(rejection.getRejectionReason()).thenReturn(reason);

    useCase.execute(requestId, userId);

    final ArgumentCaptor<RequestRejectedPayload> payloadCaptor =
        ArgumentCaptor.forClass(RequestRejectedPayload.class);
    verify(requestRepository).read(requestId);
    verify(requestRepository).markAsRejected(eq(requestId), payloadCaptor.capture());
    verify(requestRepository, never()).markAsNew(any(UUID.class));
    verify(requestRepository, never())
        .markAsAccepted(any(UUID.class), any(RequestEvaluatedPayload.class));
    assertEquals(evaluatedAt, payloadCaptor.getValue().getEvaluatedAt());
    assertEquals(evaluatorId, payloadCaptor.getValue().getEvaluatorId());
    assertEquals(reason, payloadCaptor.getValue().getRejectionReason());
  }

  @Test
  void shouldRejectReversingReturnedRequestForAnotherUser() {
    final UUID requestId = UUID.randomUUID();
    final UUID returnerId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(returnedRequest));
    when(returnedRequest.getReturnerId()).thenReturn(returnerId);

    assertThrows(UnauthorizedRequestException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verifyNoReverseOperation();
  }

  @Test
  void shouldRejectReversingReceivedRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(receivedRequest));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verifyNoReverseOperation();
  }

  @Test
  void shouldRejectReversingCompletedRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(completedRequest));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verifyNoReverseOperation();
  }

  @Test
  void shouldRejectReversingReturnedRequestWithUnknownEvaluation() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(returnedRequest));
    when(returnedRequest.getReturnerId()).thenReturn(userId);
    when(returnedRequest.getEvaluation()).thenReturn(evaluation);

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verifyNoReverseOperation();
  }

  private void verifyNoReverseOperation() {
    verify(requestRepository, never()).markAsNew(any(UUID.class));
    verify(requestRepository, never())
        .markAsAccepted(any(UUID.class), any(RequestEvaluatedPayload.class));
    verify(requestRepository, never())
        .markAsRejected(any(UUID.class), any(RequestRejectedPayload.class));
  }
}
