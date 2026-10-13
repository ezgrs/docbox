package dev.enzosantos.docbox.applications.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.requests.AbstractRequestEvaluatedPayload;
import java.time.Instant;
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

  @InjectMocks private CompleteRequestUseCase useCase;

  @Test
  void shouldMarkRequestAsAcceptedWithProvidedValues() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final Instant acceptedAt = Instant.parse("2026-10-12T12:00:00Z");

    when(timeProvider.provideInstant()).thenReturn(acceptedAt);

    useCase.execute(requestId, userId);

    final ArgumentCaptor<AbstractRequestEvaluatedPayload> payloadCaptor =
        ArgumentCaptor.forClass(AbstractRequestEvaluatedPayload.class);
    verify(requestRepository).markAsAccepted(eq(requestId), payloadCaptor.capture());
    verify(timeProvider).provideInstant();

    final AbstractRequestEvaluatedPayload payload = payloadCaptor.getValue();
    assertEquals(acceptedAt, payload.getEvaluatedAt());
    assertEquals(userId, payload.getEvaluatorId());
  }
}
