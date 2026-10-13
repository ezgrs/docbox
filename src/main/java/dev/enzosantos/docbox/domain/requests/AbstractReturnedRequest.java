package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;

public interface AbstractReturnedRequest extends AbstractEvaluatedRequest {
  Instant getReturnedAt();

  String getReturnerId();
}
