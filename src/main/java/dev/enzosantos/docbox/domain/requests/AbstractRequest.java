package dev.enzosantos.docbox.domain.requests;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AbstractRequest {
  UUID getId();

  String getName();

  Optional<String> getDescription();

  Instant getCreatedAt();

  Instant getUpdatedAt();

  String getCreatorId();
}
