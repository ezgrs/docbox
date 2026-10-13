package dev.enzosantos.docbox.applications.usecases;

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
import dev.enzosantos.docbox.domain.payloads.AbstractRequestRejectedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractAcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractEvaluatedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
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
class RejectRequestUseCaseTest {

  @Mock private RequestRepository requestRepository;

  @Mock private TimeProvider timeProvider;

  @Mock private AbstractAcknowledgedRequest acknowledgedRequest;

  @Mock private AbstractEvaluatedRequest evaluatedRequest;

  @Mock private AbstractRequest request;

  @InjectMocks private RejectRequestUseCase useCase;

  @Test
  void shouldMarkAcknowledgedRequestAsRejectedWithProvidedValues()
      throws InvalidRequestTransitionException {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant rejectedAt = Instant.parse("2026-10-12T12:00:00Z");
    final String reason = "Document is incomplete";

    when(requestRepository.read(requestId)).thenReturn(Optional.of(acknowledgedRequest));
    when(timeProvider.provideInstant()).thenReturn(rejectedAt);

    useCase.execute(requestId, userId, reason);

    final ArgumentCaptor<AbstractRequestRejectedPayload> payloadCaptor =
        ArgumentCaptor.forClass(AbstractRequestRejectedPayload.class);
    verify(requestRepository).read(requestId);
    verify(requestRepository).markAsRejected(eq(requestId), payloadCaptor.capture());
    verify(timeProvider).provideInstant();

    final AbstractRequestRejectedPayload payload = payloadCaptor.getValue();
    assertEquals(rejectedAt, payload.getEvaluatedAt());
    assertEquals(userId, payload.getEvaluatorId());
    assertEquals(reason, payload.getRejectionReason());
  }

  @Test
  void shouldRejectRejectingRequestThatIsNotAcknowledged() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final String reason = "Document is incomplete";

    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));

    assertThrows(
        InvalidRequestTransitionException.class,
        () -> useCase.execute(requestId, userId, reason));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsRejected(any(UUID.class), any(AbstractRequestRejectedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectRejectingEvaluatedRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final String reason = "Document is incomplete";

    when(requestRepository.read(requestId)).thenReturn(Optional.of(evaluatedRequest));

    assertThrows(
        InvalidRequestTransitionException.class,
        () -> useCase.execute(requestId, userId, reason));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsRejected(any(UUID.class), any(AbstractRequestRejectedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectRejectingRequestThatDoesNotExist() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final String reason = "Document is incomplete";

    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(
        InvalidRequestTransitionException.class,
        () -> useCase.execute(requestId, userId, reason));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsRejected(any(UUID.class), any(AbstractRequestRejectedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }
}
