package dev.enzosantos.docbox.applications.use_cases;

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

public class RemoveSignatureUseCase {

  private final RequestRepository requestRepository;
  private final AttachmentRepository attachmentRepository;
  private final FileStorage fileStorage;

  public RemoveSignatureUseCase(
      RequestRepository requestRepository,
      AttachmentRepository attachmentRepository,
      FileStorage fileStorage) {
    this.requestRepository = requestRepository;
    this.attachmentRepository = attachmentRepository;
    this.fileStorage = fileStorage;
  }

  public void execute(UUID id, UUID userId)
      throws AttachmentNotFoundException,
          RequestNotFoundException,
          InvalidRequestTransitionException,
          UnauthorizedRequestException {
    final Optional<Attachment> signatureResult = attachmentRepository.read(id);
    if (signatureResult.isEmpty()) {
      throw new AttachmentNotFoundException();
    }

    final Attachment signature = signatureResult.get();
    if (signature.getOriginalAttachmentId().isEmpty()) {
      throw new InvalidRequestTransitionException();
    }

    final UUID requestId = signature.getRequestId();
    final Optional<Request> requestResult = requestRepository.read(requestId);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final Request request = requestResult.get();
    if (!(request instanceof AcknowledgedRequest)
        || request instanceof EvaluatedRequest) {
      throw new InvalidRequestTransitionException();
    }

    if (!userId.equals(signature.getCreatorId())) {
      throw new UnauthorizedRequestException();
    }

    this.fileStorage.delete(
        String.format("attachments/%s/signed/%s", requestId.toString(), id.toString()));
    this.attachmentRepository.delete(id);
  }
}
