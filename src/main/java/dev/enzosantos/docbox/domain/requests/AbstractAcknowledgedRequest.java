package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;
import java.util.UUID;

public interface AbstractAcknowledgedRequest extends AbstractSentRequest {
  Instant getAcknowledgedAt();

  UUID getAcknowledgerId();
}
