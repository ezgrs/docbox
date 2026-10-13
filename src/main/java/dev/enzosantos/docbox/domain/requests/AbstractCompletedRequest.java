package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;
import java.util.UUID;

public interface AbstractCompletedRequest extends AbstractReceivedRequest {
  Instant getCompletedAt();

  UUID getCompleterId();
}
