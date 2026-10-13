package dev.enzosantos.docbox.applications.use_cases;

import dev.enzosantos.docbox.applications.ports.AttachmentRepository;
import dev.enzosantos.docbox.applications.ports.FileStorage;
import dev.enzosantos.docbox.applications.ports.RequestRepository;
import dev.enzosantos.docbox.domain.attachments.AbstractAttachment;
import dev.enzosantos.docbox.domain.attachments.AttachmentType;
import dev.enzosantos.docbox.domain.exceptions.AttachmentNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.InvalidRequestTransitionException;
import dev.enzosantos.docbox.domain.exceptions.RequestNotFoundException;
import dev.enzosantos.docbox.domain.exceptions.UnauthorizedRequestException;
import dev.enzosantos.docbox.domain.request_evaluations.AbstractRequestRejection;
import dev.enzosantos.docbox.domain.requests.AbstractReceivedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractSentRequest;
import java.util.Optional;
import java.util.UUID;

public class RemoveAttachmentUseCase {

  private final RequestRepository requestRepository;
  private final AttachmentRepository attachmentRepository;
  private final FileStorage fileStorage;

  public RemoveAttachmentUseCase(
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
    final Optional<AbstractAttachment> attachmentResult = attachmentRepository.read(id);
    if (attachmentResult.isEmpty()) {
      throw new AttachmentNotFoundException();
    }

    final AbstractAttachment attachment = attachmentResult.get();

    // Permite apenas anexos originais
    if (attachment.getType() != AttachmentType.ORIGINAL) {
      throw new InvalidRequestTransitionException();
    }

    final UUID requestId = attachment.getRequestId();
    final Optional<AbstractRequest> requestResult = requestRepository.read(requestId);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final AbstractRequest request = requestResult.get();

    final boolean isDraft = !(request instanceof AbstractSentRequest);
    final boolean isRejected =
        request instanceof AbstractReceivedRequest receivedRequest
            && (receivedRequest.getEvaluation() instanceof AbstractRequestRejection);
    // Permite apenas solicitações ainda não enviadas ou solicitações negadas recebidas
    if (!isDraft && !isRejected) {
      throw new InvalidRequestTransitionException();
    }

    // Permite apenas criador do anexo removê-lo
    if (!userId.equals(attachment.getCreatorId())) {
      throw new UnauthorizedRequestException();
    }

    this.fileStorage.delete(
        String.format("attachments/%s/original/%s", requestId.toString(), id.toString()));
    this.attachmentRepository.delete(id);
  }
}
