package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;
import java.util.UUID;

public interface CompletedRequest extends ReceivedRequest {
  Instant getCompletedAt();

  UUID getCompleterId();
}
