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
import dev.enzosantos.docbox.domain.payloads.RequestReceivedPayload;
import dev.enzosantos.docbox.domain.requests.ReceivedRequest;
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
class ReceiveRequestUseCaseTest {

  @Mock private RequestRepository requestRepository;

  @Mock private TimeProvider timeProvider;

  @Mock private ReturnedRequest returnedRequest;

  @Mock private ReceivedRequest receivedRequest;

  @Mock private Request request;

  @InjectMocks private ReceiveRequestUseCase useCase;

  @Test
  void shouldMarkReturnedRequestAsReceivedWithProvidedValues()
      throws RequestNotFoundException, InvalidRequestTransitionException {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant receivedAt = Instant.parse("2026-10-12T12:00:00Z");

    when(requestRepository.read(requestId)).thenReturn(Optional.of(returnedRequest));
    when(timeProvider.provideInstant()).thenReturn(receivedAt);

    useCase.execute(requestId, userId);

    final ArgumentCaptor<RequestReceivedPayload> payloadCaptor =
        ArgumentCaptor.forClass(RequestReceivedPayload.class);
    verify(requestRepository).read(requestId);
    verify(requestRepository).markAsReceived(eq(requestId), payloadCaptor.capture());
    verify(timeProvider).provideInstant();

    final RequestReceivedPayload payload = payloadCaptor.getValue();
    assertEquals(receivedAt, payload.getReceivedAt());
    assertEquals(userId, payload.getReceiverId());
  }

  @Test
  void shouldRejectReceivingRequestThatIsNotReturned() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsReceived(any(UUID.class), any(RequestReceivedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectReceivingAlreadyReceivedRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(receivedRequest));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsReceived(any(UUID.class), any(RequestReceivedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectReceivingRequestThatDoesNotExist() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(RequestNotFoundException.class, () -> useCase.execute(requestId, userId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .markAsReceived(any(UUID.class), any(RequestReceivedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }
}
