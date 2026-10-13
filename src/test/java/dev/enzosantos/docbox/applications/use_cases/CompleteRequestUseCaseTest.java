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
import dev.enzosantos.docbox.domain.payloads.AbstractRequestCompletedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractCompletedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractReceivedRequest;
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
class CompleteRequestUseCaseTest {

  @Mock private RequestRepository requestRepository;

  @Mock private TimeProvider timeProvider;

  @Mock private AbstractReceivedRequest receivedRequest;

  @Mock private AbstractCompletedRequest completedRequest;

  @Mock private AbstractRequest request;

  @InjectMocks private CompleteRequestUseCase useCase;

  @Test
  void shouldMarkReceivedRequestAsCompletedWithProvidedValues()
      throws RequestNotFoundException, InvalidRequestTransitionException {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant completedAt = Instant.parse("2026-10-12T12:00:00Z");

    when(requestRepository.read(requestId)).thenReturn(Optional.of(receivedRequest));
    when(timeProvider.provideInstant()).thenReturn(completedAt);

    useCase.execute(requestId, userId);

    final ArgumentCaptor<AbstractRequestCompletedPayload> payloadCaptor =
        ArgumentCaptor.forClass(AbstractRequestCompletedPayload.class);
    verify(requestRepository).read(requestId);
    verify(requestRepository).markAsCompleted(eq(requestId), payloadCaptor.capture());
    verify(timeProvider).provideInstant();

    final AbstractRequestCompletedPayload payload = payloadCaptor.getValue();
    assertEquals(completedAt, payload.getCompletedAt());
    assertEquals(userId, payload.getCompleterId());
  }

  @Test
  void shouldRejectCompletingRequestThatIsNotReceived() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsCompleted(any(UUID.class), any(AbstractRequestCompletedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectCompletingAlreadyCompletedRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(completedRequest));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsCompleted(any(UUID.class), any(AbstractRequestCompletedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectCompletingRequestThatDoesNotExist() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(RequestNotFoundException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsCompleted(any(UUID.class), any(AbstractRequestCompletedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }
}
