package dev.enzosantos.docbox.applications.use_cases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import dev.enzosantos.docbox.domain.exceptions.AttachmentNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.UnauthorizedRequestException;
import dev.enzosantos.docbox.domain.requests.AbstractAcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractEvaluatedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
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
class AddSignatureUseCaseTest {

  @Mock private AttachmentRepository attachmentRepository;

  @Mock private RequestRepository requestRepository;

  @Mock private IdentityProvider identityProvider;

  @Mock private TimeProvider timeProvider;

  @Mock private FileStorage fileStorage;

  @Mock private AbstractAttachment originalAttachment;

  @Mock private AbstractAttachment signedAttachment;

  @Mock private AbstractAcknowledgedRequest acknowledgedRequest;

  @Mock private AbstractEvaluatedRequest evaluatedRequest;

  @Mock private AbstractRequest unacknowledgedRequest;

  @InjectMocks private AddSignatureUseCase useCase;

  @Test
  void shouldCreateSignedAttachmentForAcknowledger() throws Exception {
    final UUID originalId = UUID.randomUUID();
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();
    final UUID signedId = UUID.randomUUID();
    final Instant signedAt = Instant.parse("2026-10-13T12:00:00Z");
    final InputStream stream = new ByteArrayInputStream(new byte[] {1, 2, 3});

    stubOriginalAttachment(requestId);
    when(attachmentRepository.read(originalId)).thenReturn(Optional.of(originalAttachment));
    when(originalAttachment.getName()).thenReturn("document.pdf");
    when(requestRepository.read(requestId)).thenReturn(Optional.of(acknowledgedRequest));
    when(acknowledgedRequest.getAcknowledgerId()).thenReturn(userId);
    when(identityProvider.provideUuid()).thenReturn(signedId);
    when(timeProvider.provideInstant()).thenReturn(signedAt);

    assertDoesNotThrow(() -> useCase.execute(originalId, stream, userId));

    final ArgumentCaptor<AbstractAttachment> attachmentCaptor =
        ArgumentCaptor.forClass(AbstractAttachment.class);
    final String expectedPath =
        String.format("attachments/%s/signed/%s", requestId, signedId);
    final InOrder inOrder = inOrder(fileStorage, attachmentRepository);
    inOrder.verify(fileStorage).upload(eq(expectedPath), same(stream));
    inOrder.verify(attachmentRepository).create(attachmentCaptor.capture());

    final AbstractAttachment signed = attachmentCaptor.getValue();
    assertEquals(signedId, signed.getId());
    assertEquals("document.pdf", signed.getName());
    assertEquals(requestId, signed.getRequestId());
    assertEquals(signedAt, signed.getCreatedAt());
    assertEquals(userId, signed.getCreatorId());
    assertEquals(Optional.of(originalId), signed.getOriginalAttachmentId());
  }

  @Test
  void shouldRejectWhenOriginalAttachmentDoesNotExist() {
    final UUID originalId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    when(attachmentRepository.read(originalId)).thenReturn(Optional.empty());

    assertThrows(
        AttachmentNotFoundException.class,
        () -> useCase.execute(originalId, new ByteArrayInputStream(new byte[0]), userId));

    verifyNoCreationSideEffects();
    verify(requestRepository, never()).read(any(UUID.class));
  }

  @Test
  void shouldRejectWhenRequestDoesNotExist() {
    final UUID originalId = UUID.randomUUID();
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    stubOriginalAttachment(requestId);
    when(attachmentRepository.read(originalId)).thenReturn(Optional.of(originalAttachment));
    when(requestRepository.read(requestId)).thenReturn(Optional.empty());

    assertThrows(
        RequestNotFoundException.class,
        () -> useCase.execute(originalId, new ByteArrayInputStream(new byte[0]), userId));

    verifyNoCreationSideEffects();
  }

  @Test
  void shouldRejectAlreadySignedAttachment() {
    final UUID originalId = UUID.randomUUID();

    when(attachmentRepository.read(originalId)).thenReturn(Optional.of(signedAttachment));
    when(signedAttachment.getOriginalAttachmentId()).thenReturn(Optional.of(UUID.randomUUID()));

    assertThrows(
        InvalidRequestTransitionException.class,
        () -> useCase.execute(originalId, new ByteArrayInputStream(new byte[0]), UUID.randomUUID()));

    verifyNoCreationSideEffects();
    verify(requestRepository, never()).read(any(UUID.class));
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
  void shouldRejectNonAcknowledger() {
    final UUID originalId = UUID.randomUUID();
    final UUID requestId = UUID.randomUUID();
    final UUID acknowledgerId = UUID.randomUUID();

    stubOriginalAttachment(requestId);
    when(attachmentRepository.read(originalId)).thenReturn(Optional.of(originalAttachment));
    when(requestRepository.read(requestId)).thenReturn(Optional.of(acknowledgedRequest));
    when(acknowledgedRequest.getAcknowledgerId()).thenReturn(acknowledgerId);

    assertThrows(
        UnauthorizedRequestException.class,
        () -> useCase.execute(originalId, new ByteArrayInputStream(new byte[0]), UUID.randomUUID()));

    verifyNoCreationSideEffects();
  }

  private void assertRejectsForRequest(AbstractRequest request) {
    final UUID originalId = UUID.randomUUID();
    final UUID requestId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    stubOriginalAttachment(requestId);
    when(attachmentRepository.read(originalId)).thenReturn(Optional.of(originalAttachment));
    when(requestRepository.read(requestId)).thenReturn(Optional.of(request));

    assertThrows(
        InvalidRequestTransitionException.class,
        () -> useCase.execute(originalId, new ByteArrayInputStream(new byte[0]), userId));

    verifyNoCreationSideEffects();
  }

  private void stubOriginalAttachment(UUID requestId) {
    when(originalAttachment.getRequestId()).thenReturn(requestId);
    when(originalAttachment.getOriginalAttachmentId()).thenReturn(Optional.empty());
  }

  private void verifyNoCreationSideEffects() {
    verify(identityProvider, never()).provideUuid();
    verify(timeProvider, never()).provideInstant();
    verify(fileStorage, never()).upload(anyString(), any(InputStream.class));
    verify(attachmentRepository, never()).create(any(AbstractAttachment.class));
  }
}
