package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;
import java.util.UUID;

public interface SentRequest extends Request {
  Instant getSentAt();

  UUID getSenderId();
}
