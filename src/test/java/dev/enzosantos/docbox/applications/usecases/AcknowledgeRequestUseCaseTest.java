package dev.enzosantos.docbox.applications.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestAcknowledgedPayload;
import java.time.Instant;
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

  @InjectMocks private AcknowledgeRequestUseCase useCase;

  @Test
  void shouldMarkRequestAsAcknowledgedWithProvidedValues() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant acknowledgedAt = Instant.parse("2026-10-12T12:00:00Z");

    when(timeProvider.provideInstant()).thenReturn(acknowledgedAt);

    useCase.execute(requestId, userId);

    final ArgumentCaptor<AbstractRequestAcknowledgedPayload> payloadCaptor =
        ArgumentCaptor.forClass(AbstractRequestAcknowledgedPayload.class);
    verify(requestRepository).markAsAcknowledged(eq(requestId), payloadCaptor.capture());
    verify(timeProvider).provideInstant();

    final AbstractRequestAcknowledgedPayload payload = payloadCaptor.getValue();
    assertEquals(acknowledgedAt, payload.getAcknowledgedAt());
    assertEquals(userId, payload.getAcknowledgerId());
  }
}
