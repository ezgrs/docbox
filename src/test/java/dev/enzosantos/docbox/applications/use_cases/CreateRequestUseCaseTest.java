package dev.enzosantos.docbox.applications.use_cases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.enzosantos.docbox.applications.ports.IdentityProvider;
import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
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
class CreateRequestUseCaseTest {

  @Mock private RequestRepository requestRepository;

  @Mock private IdentityProvider identityProvider;

  @Mock private TimeProvider timeProvider;

  @InjectMocks private CreateRequestUseCase useCase;

  @Test
  void shouldCreateRequestWithoutDescription() {
    final UUID id = UUID.randomUUID();
    final Instant createdAt = Instant.parse("2026-10-12T00:00:00Z");
    final String name = "Document request 1";
    final Optional<String> description = Optional.empty();
    final String userId = "user-123";

    when(identityProvider.provideUuid()).thenReturn(id);
    when(timeProvider.provideInstant()).thenReturn(createdAt);

    useCase.execute(name, description, userId);

    final ArgumentCaptor<AbstractRequest> requestCaptor =
        ArgumentCaptor.forClass(AbstractRequest.class);
    verify(requestRepository).create(requestCaptor.capture());
    verify(identityProvider).provideUuid();
    verify(timeProvider).provideInstant();

    final AbstractRequest request = requestCaptor.getValue();
    assertEquals(id, request.getId());
    assertEquals(name, request.getName());
    assertEquals(description, request.getDescription());
    assertEquals(createdAt, request.getCreatedAt());
    assertEquals(userId, request.getCreatorId());
  }

  @Test
  void shouldCreateRequestWithDescription() {
    final UUID id = UUID.randomUUID();
    final Instant createdAt = Instant.parse("2026-10-12T12:00:00Z");
    final String name = "Document request 2";
    final Optional<String> description = Optional.of("Request description");
    final String userId = "user-456";

    when(identityProvider.provideUuid()).thenReturn(id);
    when(timeProvider.provideInstant()).thenReturn(createdAt);

    useCase.execute(name, description, userId);

    final ArgumentCaptor<AbstractRequest> requestCaptor =
        ArgumentCaptor.forClass(AbstractRequest.class);
    verify(requestRepository).create(requestCaptor.capture());
    verify(identityProvider).provideUuid();
    verify(timeProvider).provideInstant();

    final AbstractRequest request = requestCaptor.getValue();
    assertEquals(id, request.getId());
    assertEquals(name, request.getName());
    assertEquals(description, request.getDescription());
    assertEquals(createdAt, request.getCreatedAt());
    assertEquals(userId, request.getCreatorId());
  }
}
