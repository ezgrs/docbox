package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;

public interface AbstractCompletedRequest extends AbstractReceivedRequest {
  Instant getCompletedAt();

  String getCompleterId();
}
