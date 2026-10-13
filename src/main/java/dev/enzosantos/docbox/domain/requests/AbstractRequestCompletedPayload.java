package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;
import java.util.UUID;

public interface AbstractRequestCompletedPayload {
  Instant getCompletedAt();

  UUID getCompleterId();
}
