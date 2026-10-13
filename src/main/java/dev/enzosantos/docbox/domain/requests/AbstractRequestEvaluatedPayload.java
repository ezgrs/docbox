package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;
import java.util.UUID;

public interface AbstractRequestEvaluatedPayload {
  Instant getEvaluatedAt();

  UUID getEvaluatorId();
}
