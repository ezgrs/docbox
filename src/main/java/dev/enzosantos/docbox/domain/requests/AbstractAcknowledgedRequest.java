package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;

public interface AbstractAcknowledgedRequest extends AbstractRequestSentPayload {
  Instant getAcknowledgedAt();

  String getAcknowledgerId();
}
