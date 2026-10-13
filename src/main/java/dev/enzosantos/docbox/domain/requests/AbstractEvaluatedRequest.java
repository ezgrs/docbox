package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;

public interface AbstractEvaluatedRequest extends AbstractRequestAcknowledgedPayload {
  Instant getEvaluatedAt();

  String getEvaluatorId();
}
