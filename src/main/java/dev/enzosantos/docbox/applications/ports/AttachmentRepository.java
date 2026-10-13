package dev.enzosantos.docbox.applications.ports;

import dev.enzosantos.docbox.domain.attachments.AbstractAttachment;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepository {
  void create(AbstractAttachment attachment);

  Optional<AbstractAttachment> read(UUID id);

  void delete(UUID id);
}
