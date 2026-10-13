package dev.enzosantos.docbox.applications.use_cases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.requests.AbstractCompletedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractSentRequest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteRequestUseCaseTest {

  @Mock private RequestRepository requestRepository;

  @Mock private AbstractRequest request;

  @Mock private AbstractSentRequest sentRequest;

  @Mock private AbstractCompletedRequest completedRequest;

  @InjectMocks private DeleteRequestUseCase useCase;

  @Test
  void shouldDeleteStrictlyAbstractRequest() {
    final UUID requestId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));

    assertDoesNotThrow(() -> useCase.execute(requestId));

    verify(requestRepository).read(requestId);
    verify(requestRepository).delete(eq(requestId));
  }

  @Test
  void shouldRejectDeletingSentRequest() {
    final UUID requestId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(sentRequest));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never()).delete(any(UUID.class));
  }

  @Test
  void shouldRejectDeletingCompletedRequest() {
    final UUID requestId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(completedRequest));

    assertThrows(InvalidRequestTransitionException.class, () -> useCase.execute(requestId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never()).delete(any(UUID.class));
  }

  @Test
  void shouldRejectDeletingRequestThatDoesNotExist() {
    final UUID requestId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(RequestNotFoundException.class, () -> useCase.execute(requestId));

    verify(requestRepository).read(requestId);
    verify(requestRepository, never()).delete(any(UUID.class));
  }
}
