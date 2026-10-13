package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;
import java.util.UUID;

public interface AcknowledgedRequest extends SentRequest {
  Instant getAcknowledgedAt();

  UUID getAcknowledgerId();
}
