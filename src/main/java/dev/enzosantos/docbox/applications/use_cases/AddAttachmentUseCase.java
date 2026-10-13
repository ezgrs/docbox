package dev.enzosantos.docbox.applications.use_cases;

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
import dev.enzosantos.docbox.domain.request_evaluations.AbstractRequestRejection;
import dev.enzosantos.docbox.domain.requests.AbstractReceivedRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractSentRequest;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class AddAttachmentUseCase {

  private final RequestRepository requestRepository;
  private final AttachmentRepository attachmentRepository;
  private final IdentityProvider identityProvider;
  private final TimeProvider timeProvider;
  private final FileStorage fileStorage;

  public AddAttachmentUseCase(
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

  public void execute(String name, InputStream stream, UUID requestId, UUID userId)
      throws RequestNotFoundException,
          InvalidRequestTransitionException,
          UnauthorizedRequestException {
    final Optional<AbstractRequest> requestResult = requestRepository.read(requestId);
    if (requestResult.isEmpty()) {
      throw new RequestNotFoundException();
    }

    final AbstractRequest request = requestResult.get();
    final boolean isDraft = !(request instanceof AbstractSentRequest);
    final boolean isRejected =
        request instanceof AbstractReceivedRequest receivedRequest
            && (receivedRequest.getEvaluation() instanceof AbstractRequestRejection);
    // Permite solicitações ainda não enviadas ou solicitações negadas recebidas
    if (!isDraft && !isRejected) {
      throw new InvalidRequestTransitionException();
    }

    // Permite apenas criador da solicitação anexar arquivos
    if (!userId.equals(request.getCreatorId())) {
      throw new UnauthorizedRequestException();
    }

    final UUID id = identityProvider.provideUuid();
    final Instant createdAt = timeProvider.provideInstant();

    this.fileStorage.upload(
        String.format("attachments/%s/original/%s", requestId.toString(), id.toString()), stream);
    this.attachmentRepository.create(
        new AbstractAttachment() {
          @Override
          public UUID getId() {
            return id;
          }

          @Override
          public String getName() {
            return name;
          }

          @Override
          public UUID getRequestId() {
            return requestId;
          }

          @Override
          public Instant getCreatedAt() {
            return createdAt;
          }

          @Override
          public UUID getCreatorId() {
            return userId;
          }

          @Override
          public AttachmentType getType() {
            return AttachmentType.ORIGINAL;
          }
        });
  }
}
