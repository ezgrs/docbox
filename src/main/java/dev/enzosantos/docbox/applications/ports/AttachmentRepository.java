package dev.enzosantos.docbox.applications.ports;

import dev.enzosantos.docbox.domain.attachments.AbstractAttachment;

public interface AttachmentRepository {
  void create(AbstractAttachment attachment);
}
