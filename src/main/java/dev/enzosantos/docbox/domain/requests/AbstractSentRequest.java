package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;

public interface AbstractSentRequest extends AbstractRequest {
  Instant getSentAt();

  String getSenderId();
}
