package dev.enzosantos.docbox.domain.requests;

import dev.enzosantos.docbox.domain.request_evaluations.RequestEvaluation;
import java.time.Instant;
import java.util.UUID;

public interface EvaluatedRequest extends AcknowledgedRequest {
  Instant getEvaluatedAt();

  UUID getEvaluatorId();

  RequestEvaluation getEvaluation();
}
