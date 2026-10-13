package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.AttachmentRepository;
import dev.enzosantos.docbox.applications.ports.FileStorage;
import dev.enzosantos.docbox.applications.ports.IdentityProvider;
import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.applications.ports.TimeProvider;
import dev.enzosantos.docbox.domain.attachments.Attachment;
import dev.enzosantos.docbox.domain.exceptions.AttachmentNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.UnauthorizedRequestException;
import dev.enzosantos.docbox.domain.requests.AcknowledgedRequest;
import dev.enzosantos.docbox.domain.requests.EvaluatedRequest;
import dev.enzosantos.docbox.domain.requests.Request;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class AddSignatureUseCase {

  private final RequestRepository requestRepository;
  private final AttachmentRepository attachmentRepository;
  private final IdentityProvider identityProvider;
  private final TimeProvider timeProvider;
  private final FileStorage fileStorage;

  public AddSignatureUseCase(
      RequestRepository requestRepository,
      AttachmentRepository attachmentRepository,
      IdentityProvider identityProvider,
      TimeProvider timeProvider,
      FileStorage fileStorage) {
    this.requestRepository = requestRepository;
    this.attachmentRepository = attachmentRepository;
    this.identityProvider = identityProvider;
    this.timeProvider = timeProvider;
    this.fileStorage = fileStorage;
  }

  public void execute(UUID originalId, InputStream stream, UUID userId)
      throws AttachmentNotFoundException,
          RequestNotFoundException,
          InvalidRequestTransitionException,
          UnauthorizedRequestException {
    final Optional<Attachment> originalAttachmentResult =
        attachmentRepository.read(originalId);
    if (originalAttachmentResult.isEmpty()) {
      throw new AttachmentNotFoundException();
    }

    final Attachment originalAttachment = originalAttachmentResult.get();
    if (originalAttachment.getOriginalAttachmentId().isPresent()) {
      throw new InvalidRequestTransitionException();
    }

    final UUID requestId = originalAttachment.getRequestId();
    final Optional<Request> requestResult = requestRepository.read(requestId);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final Request request = requestResult.get();
    if (!(request instanceof AcknowledgedRequest)
        || request instanceof EvaluatedRequest) {
      throw new InvalidRequestTransitionException();
    }

    final AcknowledgedRequest acknowledgedRequest =
        (AcknowledgedRequest) request;
    if (!userId.equals(acknowledgedRequest.getAcknowledgerId())) {
      throw new UnauthorizedRequestException();
    }

    final UUID id = identityProvider.provideUuid();
    final Instant signedAt = timeProvider.provideInstant();
    this.fileStorage.upload(
        String.format("attachments/%s/signed/%s", requestId.toString(), id.toString()), stream);
    this.attachmentRepository.create(
        new Attachment() {
          @Override
          public UUID getId() {
            return id;
          }

          @Override
          public String getName() {
            return originalAttachment.getName();
          }

          @Override
          public UUID getRequestId() {
            return requestId;
          }

          @Override
          public Instant getCreatedAt() {
            return signedAt;
          }

          @Override
          public UUID getCreatorId() {
            return userId;
          }

          @Override
          public Optional<UUID> getOriginalAttachmentId() {
            return Optional.of(originalId);
          }
        });
  }
}
