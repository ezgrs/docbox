package dev.enzosantos.docbox.domain.requests;

import dev.enzosantos.docbox.domain.request_evaluations.AbstractRequestEvaluation;
import java.time.Instant;
import java.util.UUID;

public interface AbstractEvaluatedRequest extends AbstractAcknowledgedRequest {
  Instant getEvaluatedAt();

  UUID getEvaluatorId();

  AbstractRequestEvaluation getEvaluation();
}
