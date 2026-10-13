package dev.enzosantos.docbox.domain.payloads;

import java.time.Instant;
import java.util.UUID;

public interface RequestCompletedPayload {
  Instant getCompletedAt();

  UUID getCompleterId();
}
