package dev.enzosantos.docbox.applications.use_cases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.enzosantos.docbox.applications.ports.AttachmentRepository;
import dev.enzosantos.docbox.applications.ports.FileStorage;
import dev.enzosantos.docbox.applications.ports.IdentityProvider;
import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.attachments.AbstractAttachment;
import dev.enzosantos.docbox.domain.attachments.AttachmentType;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.UnauthorizedRequestException;
import dev.enzosantos.docbox.domain.request_evaluations.AbstractRequestApproval;
import dev.enzosantos.docbox.domain.request_evaluations.AbstractRequestRejection;
import dev.enzosantos.docbox.domain.requests.AbstractReceivedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractSentRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddAttachmentUseCaseTest {

  @Mock private RequestRepository requestRepository;

  @Mock private AttachmentRepository attachmentRepository;

  @Mock private IdentityProvider identityProvider;

  @Mock private TimeProvider timeProvider;

  @Mock private FileStorage fileStorage;

  @Mock private AbstractRequest draftRequest;

  @Mock private AbstractReceivedRequest rejectedRequest;

  @Mock private AbstractReceivedRequest approvedRequest;

  @Mock private AbstractSentRequest sentRequest;

  @Mock private AbstractRequestRejection rejection;

  @Mock private AbstractRequestApproval approval;

  @InjectMocks private AddAttachmentUseCase useCase;

  @Test
  void shouldAddAttachmentToDraftRequestForItsCreator()
      throws RequestNotFoundException,
          InvalidRequestTransitionException,
          UnauthorizedRequestException {
    final String name = "original.pdf";
    final InputStream stream = new ByteArrayInputStream(new byte[] {1, 2, 3});
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final UUID attachmentId = UUID.randomUUID();
    final Instant createdAt = Instant.parse("2026-10-13T12:00:00Z");

    when(requestRepository.read(requestId)).thenReturn(Optional.of(draftRequest));
    when(draftRequest.getCreatorId()).thenReturn(userId);
    when(identityProvider.provideUuid()).thenReturn(attachmentId);
    when(timeProvider.provideInstant()).thenReturn(createdAt);

    useCase.execute(name, stream, requestId, userId);

    final ArgumentCaptor<AbstractAttachment> attachmentCaptor =
        ArgumentCaptor.forClass(AbstractAttachment.class);
    final String expectedPath =
        String.format("attachments/%s/original/%s", requestId, attachmentId);
    final InOrder inOrder = inOrder(fileStorage, attachmentRepository);
    inOrder.verify(fileStorage).upload(eq(expectedPath), same(stream));
    inOrder.verify(attachmentRepository).create(attachmentCaptor.capture());

    final AbstractAttachment attachment = attachmentCaptor.getValue();
    assertEquals(attachmentId, attachment.getId());
    assertEquals(name, attachment.getName());
    assertEquals(requestId, attachment.getRequestId());
    assertEquals(createdAt, attachment.getCreatedAt());
    assertEquals(userId, attachment.getCreatorId());
    assertEquals(AttachmentType.ORIGINAL, attachment.getType());
    verify(requestRepository).read(requestId);
    verify(identityProvider).provideUuid();
    verify(timeProvider).provideInstant();
  }

  @Test
  void shouldAddAttachmentToRejectedReceivedRequestForItsCreator()
      throws RequestNotFoundException,
          InvalidRequestTransitionException,
          UnauthorizedRequestException {
    final InputStream stream = new ByteArrayInputStream(new byte[] {1});
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(rejectedRequest));
    when(rejectedRequest.getEvaluation()).thenReturn(rejection);
    when(rejectedRequest.getCreatorId()).thenReturn(userId);
    when(identityProvider.provideUuid()).thenReturn(UUID.randomUUID());
    when(timeProvider.provideInstant()).thenReturn(Instant.parse("2026-10-13T12:00:00Z"));

    useCase.execute("correction.pdf", stream, requestId, userId);

    verify(fileStorage).upload(any(String.class), same(stream));
    verify(attachmentRepository).create(any(AbstractAttachment.class));
  }

  @Test
  void shouldRejectAddingAttachmentToRequestThatDoesNotExist() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(
        RequestNotFoundException.class,
        () ->
            useCase.execute("file.pdf", new ByteArrayInputStream(new byte[0]), requestId, userId));

    verify(requestRepository).read(requestId);
    verifyNoSideEffects();
  }

  @Test
  void shouldRejectAddingAttachmentToSentRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(sentRequest));

    assertThrows(
        InvalidRequestTransitionException.class,
        () ->
            useCase.execute("file.pdf", new ByteArrayInputStream(new byte[0]), requestId, userId));

    verify(requestRepository).read(requestId);
    verifyNoSideEffects();
  }

  @Test
  void shouldRejectAddingAttachmentToApprovedReceivedRequest() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(approvedRequest));
    when(approvedRequest.getEvaluation()).thenReturn(approval);

    assertThrows(
        InvalidRequestTransitionException.class,
        () ->
            useCase.execute("file.pdf", new ByteArrayInputStream(new byte[0]), requestId, userId));

    verify(requestRepository).read(requestId);
    verifyNoSideEffects();
  }

  @Test
  void shouldRejectAddingAttachmentForAnotherUser() {
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(requestRepository.read(requestId)).thenReturn(Optional.of(draftRequest));
    when(draftRequest.getCreatorId()).thenReturn(UUID.randomUUID());

    assertThrows(
        UnauthorizedRequestException.class,
        () ->
            useCase.execute("file.pdf", new ByteArrayInputStream(new byte[0]), requestId, userId));

    verify(requestRepository).read(requestId);
    verifyNoSideEffects();
  }

  private void verifyNoSideEffects() {
    verify(identityProvider, never()).provideUuid();
    verify(timeProvider, never()).provideInstant();
    verify(fileStorage, never()).upload(any(String.class), any(InputStream.class));
    verify(attachmentRepository, never()).create(any(AbstractAttachment.class));
  }
}
