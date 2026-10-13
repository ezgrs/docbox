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
import dev.enzosantos.docbox.domain.attachments.Attachment;
import dev.enzosantos.docbox.domain.exceptions.AttachmentNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.UnauthorizedRequestException;
import dev.enzosantos.docbox.domain.requests.AcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.EvaluatedRequest;
import dev.enzosantos.docbox.domain.requests.Request;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RemoveSignatureUseCaseTest {

  @Mock private RequestRepository requestRepository;

  @Mock private AttachmentRepository attachmentRepository;

  @Mock private FileStorage fileStorage;

  @Mock private Attachment signature;

  @Mock private AcknowledgedRequest acknowledgedRequest;

  @Mock private EvaluatedRequest evaluatedRequest;

  @Mock private Request unacknowledgedRequest;

  @InjectMocks private RemoveSignatureUseCase useCase;

  @Test
  void shouldRemoveSignatureByItsCreator() throws Exception {
    final UUID signatureId = UUID.randomUUID();
    final UUID originalId = UUID.randomUUID();
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    stubSignature(requestId, originalId);
    when(signature.getCreatorId()).thenReturn(userId);
    when(attachmentRepository.read(signatureId)).thenReturn(Optional.of(signature));
    when(requestRepository.read(requestId)).thenReturn(Optional.of(acknowledgedRequest));

    assertDoesNotThrow(() -> useCase.execute(signatureId, userId));

    final String expectedPath =
        String.format("attachments/%s/signed/%s", requestId, signatureId);
    final InOrder inOrder = inOrder(fileStorage, attachmentRepository);
    inOrder.verify(fileStorage).delete(eq(expectedPath));
    inOrder.verify(attachmentRepository).delete(signatureId);
  }

  @Test
  void shouldRejectWhenSignatureDoesNotExist() {
    final UUID signatureId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(attachmentRepository.read(signatureId)).thenReturn(Optional.empty());

    assertThrows(
        AttachmentNotFoundException.class, () -> useCase.execute(signatureId, userId));

    verifyNoDeletion();
    verify(requestRepository, never()).read(any(UUID.class));
  }

  @Test
  void shouldRejectOriginalAttachment() {
    final UUID signatureId = UUID.randomUUID();

    when(attachmentRepository.read(signatureId)).thenReturn(Optional.of(signature));
    when(signature.getOriginalAttachmentId()).thenReturn(Optional.empty());

    assertThrows(
        InvalidRequestTransitionException.class,
        () -> useCase.execute(signatureId, UUID.randomUUID()));

    verifyNoDeletion();
    verify(requestRepository, never()).read(any(UUID.class));
  }

  @Test
  void shouldRejectWhenRequestDoesNotExist() {
    final UUID signatureId = UUID.randomUUID();
    final UUID requestId = UUID.randomUUID();
    final UUID originalId = UUID.randomUUID();

    stubSignature(requestId, originalId);
    when(attachmentRepository.read(signatureId)).thenReturn(Optional.of(signature));
    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(
        RequestNotFoundException.class,
        () -> useCase.execute(signatureId, UUID.randomUUID()));

    verifyNoDeletion();
  }

  @Test
  void shouldRejectUnacknowledgedRequest() {
    assertRejectsForRequest(unacknowledgedRequest);
  }

  @Test
  void shouldRejectEvaluatedRequest() {
    assertRejectsForRequest(evaluatedRequest);
  }

  @Test
  void shouldRejectRemovalByAnotherUser() {
    final UUID signatureId = UUID.randomUUID();
    final UUID requestId = UUID.randomUUID();
    final UUID originalId = UUID.randomUUID();

    stubSignature(requestId, originalId);
    when(signature.getCreatorId()).thenReturn(UUID.randomUUID());
    when(attachmentRepository.read(signatureId)).thenReturn(Optional.of(signature));
    when(requestRepository.read(requestId)).thenReturn(Optional.of(acknowledgedRequest));

    assertThrows(
        UnauthorizedRequestException.class,
        () -> useCase.execute(signatureId, UUID.randomUUID()));

    verifyNoDeletion();
  }

  private void assertRejectsForRequest(Request request) {
    final UUID signatureId = UUID.randomUUID();
    final UUID requestId = UUID.randomUUID();
    final UUID originalId = UUID.randomUUID();

    stubSignature(requestId, originalId);
    when(attachmentRepository.read(signatureId)).thenReturn(Optional.of(signature));
    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));

    assertThrows(
        InvalidRequestTransitionException.class,
        () -> useCase.execute(signatureId, UUID.randomUUID()));

    verifyNoDeletion();
  }

  private void stubSignature(UUID requestId, UUID originalId) {
    when(signature.getRequestId()).thenReturn(requestId);
    when(signature.getOriginalAttachmentId()).thenReturn(Optional.of(originalId));
  }

  private void verifyNoDeletion() {
    verify(fileStorage, never()).delete(anyString());
    verify(attachmentRepository, never()).delete(any(UUID.class));
  }
}
