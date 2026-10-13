package dev.enzosantos.docbox.domain.payloads;

import java.time.Instant;
import java.util.UUID;

public interface RequestReturnedPayload {
  Instant getReturnedAt();

  UUID getReturnerId();
}
