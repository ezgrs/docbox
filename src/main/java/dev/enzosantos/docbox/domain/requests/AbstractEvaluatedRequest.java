package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;

public interface AbstractEvaluatedRequest extends AbstractAcknowledgedRequest {
  Instant getEvaluatedAt();

  String getEvaluatorId();
}
