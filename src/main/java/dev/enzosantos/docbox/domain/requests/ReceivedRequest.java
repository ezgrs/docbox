package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;
import java.util.UUID;

public interface ReceivedRequest extends ReturnedRequest {
  Instant getReceivedAt();

  UUID getReceiverId();
}
