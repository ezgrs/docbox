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
import dev.enzosantos.docbox.domain.payloads.AbstractRequestSentPayload;
import dev.enzosantos.docbox.domain.request_evaluations.AbstractRequestEvaluation;
import dev.enzosantos.docbox.domain.request_evaluations.AbstractRequestRejection;
import dev.enzosantos.docbox.domain.requests.AbstractAcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractReceivedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractSentRequest;
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

  @Mock private AbstractRequest request;

  @Mock private AbstractSentRequest sentRequest;

  @Mock private AbstractAcknowledgedRequest acknowledgedRequest;

  @Mock private AbstractReceivedRequest receivedRequest;

  @Mock private AbstractRequestEvaluation evaluation;

  @InjectMocks private SendRequestUseCase useCase;

  @Test
  void shouldMarkRequestAsSentWithProvidedValues() throws InvalidRequestTransitionException {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant sentAt = Instant.parse("2026-10-12T12:00:00Z");

    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));
    when(timeProvider.provideInstant()).thenReturn(sentAt);

    useCase.execute(requestId, userId);

    final ArgumentCaptor<AbstractRequestSentPayload> payloadCaptor =
        ArgumentCaptor.forClass(AbstractRequestSentPayload.class);
    verify(requestRepository).markAsSent(eq(requestId), payloadCaptor.capture());
    verify(requestRepository).read(requestId);
    verify(timeProvider).provideInstant();

    final AbstractRequestSentPayload payload = payloadCaptor.getValue();
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
        .markAsSent(any(UUID.class), any(AbstractRequestSentPayload.class));
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
        .markAsSent(any(UUID.class), any(AbstractRequestSentPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldAllowResendingRejectedRequest() throws InvalidRequestTransitionException {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant sentAt = Instant.parse("2026-10-12T13:00:00Z");
    final AbstractRequestEvaluation rejection =
        Mockito.mock(
            AbstractRequestEvaluation.class,
            Mockito.withSettings().extraInterfaces(AbstractRequestRejection.class));

    when(requestRepository.read(requestId)).thenReturn(Optional.of(receivedRequest));
    when(receivedRequest.getEvaluation()).thenReturn(rejection);
    when(timeProvider.provideInstant()).thenReturn(sentAt);

    useCase.execute(requestId, userId);

    verify(requestRepository).read(requestId);
    verify(requestRepository).markAsSent(eq(requestId), any(AbstractRequestSentPayload.class));
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
        .markAsSent(any(UUID.class), any(AbstractRequestSentPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectSendingRequestThatDoesNotExist() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsSent(any(UUID.class), any(AbstractRequestSentPayload.class));
    verify(timeProvider, never()).provideInstant();
  }
}
