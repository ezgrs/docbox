package dev.enzosantos.docbox.applications.ports;

import dev.enzosantos.docbox.domain.attachments.Attachment;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepository {
  void create(Attachment attachment);

  Optional<Attachment> read(UUID id);

  void delete(UUID id);
}
