package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;

public interface AbstractReturnedRequest {
  Instant getReturnedAt();

  String getReturnerId();
}
