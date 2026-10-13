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
import dev.enzosantos.docbox.domain.payloads.RequestReturnedPayload;
import dev.enzosantos.docbox.domain.requests.EvaluatedRequest;
import dev.enzosantos.docbox.domain.requests.Request;
import dev.enzosantos.docbox.domain.requests.ReturnedRequest;
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
class ReturnRequestUseCaseTest {

  @Mock private RequestRepository requestRepository;

  @Mock private TimeProvider timeProvider;

  @Mock private EvaluatedRequest evaluatedRequest;

  @Mock private ReturnedRequest returnedRequest;

  @Mock private Request request;

  @InjectMocks private ReturnRequestUseCase useCase;

  @Test
  void shouldMarkEvaluatedRequestAsReturnedWithProvidedValues()
      throws RequestNotFoundException, InvalidRequestTransitionException {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant returnedAt = Instant.parse("2026-10-12T12:00:00Z");

    when(requestRepository.read(requestId)).thenReturn(Optional.of(evaluatedRequest));
    when(timeProvider.provideInstant()).thenReturn(returnedAt);

    useCase.execute(requestId, userId);

    final ArgumentCaptor<RequestReturnedPayload> payloadCaptor =
        ArgumentCaptor.forClass(RequestReturnedPayload.class);
    verify(requestRepository).read(requestId);
    verify(requestRepository).markAsReturned(eq(requestId), payloadCaptor.capture());
    verify(timeProvider).provideInstant();

    final RequestReturnedPayload payload = payloadCaptor.getValue();
    assertEquals(returnedAt, payload.getReturnedAt());
    assertEquals(userId, payload.getReturnerId());
  }

  @Test
  void shouldRejectReturningRequestThatIsNotEvaluated() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsReturned(any(UUID.class), any(RequestReturnedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectReturningAlreadyReturnedRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(returnedRequest));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsReturned(any(UUID.class), any(RequestReturnedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectReturningRequestThatDoesNotExist() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(RequestNotFoundException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsReturned(any(UUID.class), any(RequestReturnedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }
}
