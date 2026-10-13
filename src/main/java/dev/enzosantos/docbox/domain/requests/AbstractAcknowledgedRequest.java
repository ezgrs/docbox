package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;

public interface AbstractAcknowledgedRequest extends AbstractSentRequest {
  Instant getAcknowledgedAt();

  String getAcknowledgerId();
}
