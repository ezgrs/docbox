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
import dev.enzosantos.docbox.domain.payloads.AbstractRequestAcknowledgedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractAcknowledgedRequest;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AcknowledgeRequestUseCaseTest {

  @Mock private RequestRepository requestRepository;

  @Mock private TimeProvider timeProvider;

  @Mock private AbstractRequest request;

  @Mock private AbstractSentRequest sentRequest;

  @Mock private AbstractAcknowledgedRequest acknowledgedRequest;

  @InjectMocks private AcknowledgeRequestUseCase useCase;

  @Test
  void shouldMarkRequestAsAcknowledgedWithProvidedValues()
      throws RequestNotFoundException, InvalidRequestTransitionException {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant acknowledgedAt = Instant.parse("2026-10-12T12:00:00Z");

    when(requestRepository.read(requestId)).thenReturn(Optional.of(sentRequest));
    when(timeProvider.provideInstant()).thenReturn(acknowledgedAt);

    useCase.execute(requestId, userId);

    final ArgumentCaptor<AbstractRequestAcknowledgedPayload> payloadCaptor =
        ArgumentCaptor.forClass(AbstractRequestAcknowledgedPayload.class);
    verify(requestRepository).read(requestId);
    verify(requestRepository).markAsAcknowledged(eq(requestId), payloadCaptor.capture());
    verify(timeProvider).provideInstant();

    final AbstractRequestAcknowledgedPayload payload = payloadCaptor.getValue();
    assertEquals(acknowledgedAt, payload.getAcknowledgedAt());
    assertEquals(userId, payload.getAcknowledgerId());
  }

  @Test
  void shouldRejectAcknowledgingAlreadyAcknowledgedRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(acknowledgedRequest));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsAcknowledged(any(UUID.class), any(AbstractRequestAcknowledgedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectAcknowledgingRequestThatWasNotSent() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsAcknowledged(any(UUID.class), any(AbstractRequestAcknowledgedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectAcknowledgingRequestThatDoesNotExist() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(RequestNotFoundException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsAcknowledged(any(UUID.class), any(AbstractRequestAcknowledgedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }
}
