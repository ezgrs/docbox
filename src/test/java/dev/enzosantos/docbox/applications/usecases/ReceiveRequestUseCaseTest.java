package dev.enzosantos.docbox.applications.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.requests.AbstractRequestReceivedPayload;
import java.time.Instant;
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

  @InjectMocks private ReceiveRequestUseCase useCase;

  @Test
  void shouldMarkRequestAsReceivedWithProvidedValues() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant receivedAt = Instant.parse("2026-10-12T12:00:00Z");

    when(timeProvider.provideInstant()).thenReturn(receivedAt);

    useCase.execute(requestId, userId);

    final ArgumentCaptor<AbstractRequestReceivedPayload> payloadCaptor =
        ArgumentCaptor.forClass(AbstractRequestReceivedPayload.class);
    verify(requestRepository).markAsReceived(eq(requestId), payloadCaptor.capture());
    verify(timeProvider).provideInstant();

    final AbstractRequestReceivedPayload payload = payloadCaptor.getValue();
    assertEquals(receivedAt, payload.getReceivedAt());
    assertEquals(userId, payload.getReceiverId());
  }
}
