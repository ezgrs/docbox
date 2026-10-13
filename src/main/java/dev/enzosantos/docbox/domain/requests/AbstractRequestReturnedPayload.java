package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;
import java.util.UUID;

public interface AbstractRequestReturnedPayload {
  Instant getReturnedAt();

  UUID getReturnerId();
}
