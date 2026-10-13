package dev.enzosantos.docbox.domain.attachments;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AbstractAttachment {
  UUID getId();

  String getName();

  UUID getRequestId();

  Instant getCreatedAt();

  UUID getCreatorId();

  Optional<UUID> getOriginalAttachmentId();
}
