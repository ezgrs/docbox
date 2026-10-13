package dev.enzosantos.docbox.applications.use_cases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.enzosantos.docbox.applications.ports.AttachmentRepository;
import dev.enzosantos.docbox.applications.ports.FileStorage;
import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.domain.attachments.AbstractAttachment;
import dev.enzosantos.docbox.domain.exceptions.AttachmentNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.UnauthorizedRequestException;
import dev.enzosantos.docbox.domain.request_evaluations.AbstractRequestApproval;
import dev.enzosantos.docbox.domain.request_evaluations.AbstractRequestRejection;
import dev.enzosantos.docbox.domain.requests.AbstractReceivedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractReturnedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractSentRequest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RemoveAttachmentUseCaseTest {

  @Mock private AttachmentRepository attachmentRepository;

  @Mock private RequestRepository requestRepository;

  @Mock private FileStorage fileStorage;

  @Mock private AbstractAttachment attachment;

  @Mock private AbstractRequest draftRequest;

  @Mock private AbstractReceivedRequest rejectedRequest;

  @Mock private AbstractReceivedRequest approvedRequest;

  @Mock private AbstractSentRequest sentRequest;

  @Mock private AbstractReturnedRequest returnedRequest;

  @Mock private AbstractRequestRejection rejection;

  @Mock private AbstractRequestApproval approval;

  @InjectMocks private RemoveAttachmentUseCase useCase;

  @Test
  void shouldRemoveOriginalAttachmentFromDraftRequest() throws Exception {
    final UUID attachmentId = UUID.randomUUID();
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    stubOriginalAttachment(requestId);
    when(attachmentRepository.read(attachmentId)).thenReturn(Optional.of(attachment));
    when(requestRepository.read(requestId)).thenReturn(Optional.of(draftRequest));
    when(attachment.getCreatorId()).thenReturn(userId);

    assertDoesNotThrow(() -> useCase.execute(attachmentId, userId));

    verifyRemovalInOrder(attachmentId, requestId);
  }

  @Test
  void shouldRemoveOriginalAttachmentFromRejectedReceivedRequest() throws Exception {
    final UUID attachmentId = UUID.randomUUID();
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    stubOriginalAttachment(requestId);
    when(attachmentRepository.read(attachmentId)).thenReturn(Optional.of(attachment));
    when(requestRepository.read(requestId)).thenReturn(Optional.of(rejectedRequest));
    when(rejectedRequest.getEvaluation()).thenReturn(rejection);
    when(attachment.getCreatorId()).thenReturn(userId);

    assertDoesNotThrow(() -> useCase.execute(attachmentId, userId));

    verifyRemovalInOrder(attachmentId, requestId);
  }

  @Test
  void shouldRejectWhenAttachmentDoesNotExist() {
    final UUID attachmentId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(attachmentRepository.read(attachmentId)).thenReturn(Optional.empty());

    assertThrows(AttachmentNotFoundException.class, () -> useCase.execute(attachmentId, userId));

    verify(attachmentRepository).read(attachmentId);
    verifyNoRemoval();
    verify(requestRepository, never()).read(any(UUID.class));
  }

  @Test
  void shouldRejectWhenAttachmentRequestDoesNotExist() {
    final UUID attachmentId = UUID.randomUUID();
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    stubOriginalAttachment(requestId);
    when(attachmentRepository.read(attachmentId)).thenReturn(Optional.of(attachment));
    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(RequestNotFoundException.class, () -> useCase.execute(attachmentId, userId));

    verify(requestRepository).read(requestId);
    verifyNoRemoval();
  }

  @Test
  void shouldRejectSignedAttachment() {
    final UUID attachmentId = UUID.randomUUID();

    when(attachmentRepository.read(attachmentId)).thenReturn(Optional.of(attachment));
    when(attachment.getOriginalAttachmentId()).thenReturn(Optional.of(UUID.randomUUID()));

    assertThrows(
        InvalidRequestTransitionException.class,
        () -> useCase.execute(attachmentId, UUID.randomUUID()));

    verifyNoRemoval();
    verify(requestRepository, never()).read(any(UUID.class));
  }

  @Test
  void shouldRejectAttachmentFromSentRequest() {
    assertRejectsForInvalidRequest(sentRequest);
  }

  @Test
  void shouldRejectAttachmentFromApprovedReceivedRequest() {
    when(approvedRequest.getEvaluation()).thenReturn(approval);
    assertRejectsForInvalidRequest(approvedRequest);
  }

  @Test
  void shouldRejectAttachmentFromIntermediateRequest() {
    assertRejectsForInvalidRequest(returnedRequest);
  }

  @Test
  void shouldRejectRemovalByAnotherUser() {
    final UUID attachmentId = UUID.randomUUID();
    final UUID requestId = UUID.randomUUID();
    final UUID creatorId = UUID.randomUUID();

    stubOriginalAttachment(requestId);
    when(attachmentRepository.read(attachmentId)).thenReturn(Optional.of(attachment));
    when(requestRepository.read(requestId)).thenReturn(Optional.of(draftRequest));
    when(attachment.getCreatorId()).thenReturn(creatorId);

    assertThrows(
        UnauthorizedRequestException.class, () -> useCase.execute(attachmentId, UUID.randomUUID()));

    verifyNoRemoval();
  }

  private void assertRejectsForInvalidRequest(AbstractRequest request) {
    final UUID attachmentId = UUID.randomUUID();
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    stubOriginalAttachment(requestId);
    when(attachmentRepository.read(attachmentId)).thenReturn(Optional.of(attachment));
    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));

    assertThrows(
        InvalidRequestTransitionException.class, () -> useCase.execute(attachmentId, userId));

    verifyNoRemoval();
  }

  private void stubOriginalAttachment(UUID requestId) {
    when(attachment.getRequestId()).thenReturn(requestId);
    when(attachment.getOriginalAttachmentId()).thenReturn(Optional.empty());
  }

  private void verifyRemovalInOrder(UUID attachmentId, UUID requestId) {
    final String expectedPath =
        String.format("attachments/%s/original/%s", requestId, attachmentId);
    final InOrder inOrder = inOrder(fileStorage, attachmentRepository);

    inOrder.verify(fileStorage).delete(eq(expectedPath));
    inOrder.verify(attachmentRepository).delete(attachmentId);
  }

  private void verifyNoRemoval() {
    verify(fileStorage, never()).delete(anyString());
    verify(attachmentRepository, never()).delete(any(UUID.class));
  }
}
