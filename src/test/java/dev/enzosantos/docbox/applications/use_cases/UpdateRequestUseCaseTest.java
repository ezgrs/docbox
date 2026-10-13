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
import dev.enzosantos.docbox.domain.exceptions.UnauthorizedRequestException;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestUpdatedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractCompletedRequest;
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
class UpdateRequestUseCaseTest {

  @Mock private RequestRepository requestRepository;

  @Mock private TimeProvider timeProvider;

  @Mock private AbstractRequest request;

  @Mock private AbstractSentRequest sentRequest;

  @Mock private AbstractCompletedRequest completedRequest;

  @InjectMocks private UpdateRequestUseCase useCase;

  @Test
  void shouldUpdateRequestNameAndDescriptionForItsCreator()
      throws InvalidRequestTransitionException, UnauthorizedRequestException {
    final UUID requestId = UUID.randomUUID();
    final String name = "Updated request";
    final Optional<String> description = Optional.of("Updated description");
    final String userId = "creator-id";
    final Instant updatedAt = Instant.parse("2026-10-13T12:00:00Z");

    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));
    when(request.getCreatorId()).thenReturn(userId);
    when(timeProvider.provideInstant()).thenReturn(updatedAt);

    useCase.execute(requestId, name, description, userId);

    final ArgumentCaptor<AbstractRequestUpdatedPayload> payloadCaptor =
        ArgumentCaptor.forClass(AbstractRequestUpdatedPayload.class);
    verify(requestRepository).read(requestId);
    verify(requestRepository).update(eq(requestId), payloadCaptor.capture());

    final AbstractRequestUpdatedPayload payload = payloadCaptor.getValue();
    assertEquals(name, payload.getName());
    assertEquals(description, payload.getDescription());
    assertEquals(updatedAt, payload.getUpdatedAt());
    verify(timeProvider).provideInstant();
  }

  @Test
  void shouldUpdateRequestWithoutDescription()
      throws InvalidRequestTransitionException, UnauthorizedRequestException {
    final UUID requestId = UUID.randomUUID();
    final String name = "Updated request";
    final Optional<String> description = Optional.empty();
    final String userId = "creator-id";
    final Instant updatedAt = Instant.parse("2026-10-13T12:00:00Z");

    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));
    when(request.getCreatorId()).thenReturn(userId);
    when(timeProvider.provideInstant()).thenReturn(updatedAt);

    useCase.execute(requestId, name, description, userId);

    final ArgumentCaptor<AbstractRequestUpdatedPayload> payloadCaptor =
        ArgumentCaptor.forClass(AbstractRequestUpdatedPayload.class);
    verify(requestRepository).update(eq(requestId), payloadCaptor.capture());
    assertEquals(description, payloadCaptor.getValue().getDescription());
    assertEquals(updatedAt, payloadCaptor.getValue().getUpdatedAt());
    verify(timeProvider).provideInstant();
  }

  @Test
  void shouldRejectUpdatingRequestThatDoesNotExist() {
    final UUID requestId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(
        InvalidRequestTransitionException.class,
        () -> useCase.execute(requestId, "Updated request", Optional.empty(), "creator-id"));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .update(any(UUID.class), any(AbstractRequestUpdatedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectUpdatingSentRequest() {
    final UUID requestId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(sentRequest));

    assertThrows(
        InvalidRequestTransitionException.class,
        () -> useCase.execute(requestId, "Updated request", Optional.empty(), "creator-id"));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .update(any(UUID.class), any(AbstractRequestUpdatedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectUpdatingCompletedRequest() {
    final UUID requestId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(completedRequest));

    assertThrows(
        InvalidRequestTransitionException.class,
        () -> useCase.execute(requestId, "Updated request", Optional.empty(), "creator-id"));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .update(any(UUID.class), any(AbstractRequestUpdatedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }

  @Test
  void shouldRejectUpdatingRequestByAnotherUser()
      throws InvalidRequestTransitionException, UnauthorizedRequestException {
    final UUID requestId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));
    when(request.getCreatorId()).thenReturn("creator-id");

    assertThrows(
        UnauthorizedRequestException.class,
        () -> useCase.execute(requestId, "Updated request", Optional.empty(), "another-user"));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never())
        .update(any(UUID.class), any(AbstractRequestUpdatedPayload.class));
    verify(timeProvider, never()).provideInstant();
  }
}
