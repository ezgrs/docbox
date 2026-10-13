package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;

public interface AbstractReceivedRequest extends AbstractReturnedRequest {
  Instant getReceivedAt();

  String getReceiverId();
}
