package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;
import java.util.UUID;

public interface ReturnedRequest extends EvaluatedRequest {
  Instant getReturnedAt();

  UUID getReturnerId();
}
