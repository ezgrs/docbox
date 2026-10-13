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
import dev.enzosantos.docbox.domain.payloads.AbstractRequestEvaluatedPayload;
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
class AcceptRequestUseCaseTest {

  @Mock private RequestRepository requestRepository;

  @Mock private TimeProvider timeProvider;

  @Mock private AbstractAcknowledgedRequest acknowledgedRequest;

  @Mock private AbstractEvaluatedRequest evaluatedRequest;

  @Mock private AbstractRequest request;

  @InjectMocks private AcceptRequestUseCase useCase;

  @Test
  void shouldMarkAcknowledgedRequestAsAcceptedWithProvidedValues()
      throws InvalidRequestTransitionException {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant acceptedAt = Instant.parse("2026-10-12T12:00:00Z");

    when(requestRepository.read(requestId)).thenReturn(Optional.of(acknowledgedRequest));
    when(timeProvider.provideInstant()).thenReturn(acceptedAt);

    useCase.execute(requestId, userId);

    final ArgumentCaptor<AbstractRequestEvaluatedPayload> payloadCaptor =
        ArgumentCaptor.forClass(AbstractRequestEvaluatedPayload.class);
    verify(requestRepository).read(requestId);
    verify(requestRepository).markAsAccepted(eq(requestId), payloadCaptor.capture());
    verify(timeProvider).provideInstant();

    final AbstractRequestEvaluatedPayload payload = payloadCaptor.getValue();
    assertEquals(acceptedAt, payload.getEvaluatedAt());
    assertEquals(userId, payload.getEvaluatorId());
  }

  @Test
  void shouldRejectAcceptingRequestThatIsNotAcknowledged() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsAccepted(any(UUID.class), any(AbstractRequestEvaluatedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectAcceptingEvaluatedRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(evaluatedRequest));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsAccepted(any(UUID.class), any(AbstractRequestEvaluatedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectAcceptingRequestThatDoesNotExist() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsAccepted(any(UUID.class), any(AbstractRequestEvaluatedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }
}
