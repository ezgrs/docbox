package dev.enzosantos.docbox.applications.use_cases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.payloads.RequestSentPayload;
import dev.enzosantos.docbox.domain.request_evaluations.RequestEvaluation;
import dev.enzosantos.docbox.domain.request_evaluations.RequestRejection;
import dev.enzosantos.docbox.domain.requests.AcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.ReceivedRequest;
import dev.enzosantos.docbox.domain.requests.Request;
import dev.enzosantos.docbox.domain.requests.SentRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendRequestUseCaseTest {

  @Mock private RequestRepository requestRepository;

  @Mock private TimeProvider timeProvider;

  @Mock private Request request;

  @Mock private SentRequest sentRequest;

  @Mock private AcknowledgedRequest acknowledgedRequest;

  @Mock private ReceivedRequest receivedRequest;

  @Mock private RequestEvaluation evaluation;

  @InjectMocks private SendRequestUseCase useCase;

  @Test
  void shouldMarkRequestAsSentWithProvidedValues()
      throws RequestNotFoundException, InvalidRequestTransitionException {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant sentAt = Instant.parse("2026-10-12T12:00:00Z");

    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));
    when(timeProvider.provideInstant()).thenReturn(sentAt);

    useCase.execute(requestId, userId);

    final ArgumentCaptor<RequestSentPayload> payloadCaptor =
        ArgumentCaptor.forClass(RequestSentPayload.class);
    verify(requestRepository).markAsSent(eq(requestId), payloadCaptor.capture());
    verify(requestRepository).read(requestId);
    verify(timeProvider).provideInstant();

    final RequestSentPayload payload = payloadCaptor.getValue();
    assertEquals(sentAt, payload.getSentAt());
    assertEquals(userId, payload.getSenderId());
  }

  @Test
  void shouldRejectSendingAlreadySentRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(sentRequest));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsSent(any(UUID.class), any(RequestSentPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectSendingAcknowledgedRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(acknowledgedRequest));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsSent(any(UUID.class), any(RequestSentPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldAllowResendingRejectedRequest()
      throws RequestNotFoundException, InvalidRequestTransitionException {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant sentAt = Instant.parse("2026-10-12T13:00:00Z");
    final RequestEvaluation rejection =
        Mockito.mock(
            RequestEvaluation.class,
            Mockito.withSettings().extraInterfaces(RequestRejection.class));

    when(requestRepository.read(requestId)).thenReturn(Optional.of(receivedRequest));
    when(receivedRequest.getEvaluation()).thenReturn(rejection);
    when(timeProvider.provideInstant()).thenReturn(sentAt);

    useCase.execute(requestId, userId);

    verify(requestRepository).read(requestId);
    verify(requestRepository).markAsSent(eq(requestId), any(RequestSentPayload.class));
    verify(timeProvider).provideInstant();
  }

  @Test
  void shouldRejectSendingAcceptedRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(receivedRequest));
    when(receivedRequest.getEvaluation()).thenReturn(evaluation);

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsSent(any(UUID.class), any(RequestSentPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectSendingRequestThatDoesNotExist() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(RequestNotFoundException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsSent(any(UUID.class), any(RequestSentPayload.class));
    verify(timeProvider, never()).provideInstant();
  }
}
