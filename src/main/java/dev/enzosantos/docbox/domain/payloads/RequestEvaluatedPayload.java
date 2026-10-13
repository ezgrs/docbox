package dev.enzosantos.docbox.domain.payloads;

import java.time.Instant;
import java.util.UUID;

public interface RequestEvaluatedPayload {
  Instant getEvaluatedAt();

  UUID getEvaluatorId();
}
