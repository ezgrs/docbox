package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;
import java.util.UUID;

public interface AbstractReceivedRequest extends AbstractReturnedRequest {
  Instant getReceivedAt();

  UUID getReceiverId();
}
