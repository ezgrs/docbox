package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;

public interface AbstractReceivedRequest {
  Instant getReceivedAt();

  String getReceiverId();
}
