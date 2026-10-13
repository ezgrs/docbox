package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;
import java.util.UUID;

public interface AbstractSentRequest extends AbstractRequest {
  Instant getSentAt();

  UUID getSenderId();
}
