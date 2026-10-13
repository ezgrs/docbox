package dev.enzosantos.docbox.domain.payloads;

import java.time.Instant;
import java.util.Optional;

public interface AbstractRequestUpdatedPayload {
  String getName();

  Optional<String> getDescription();

  Instant getUpdatedAt();
}
