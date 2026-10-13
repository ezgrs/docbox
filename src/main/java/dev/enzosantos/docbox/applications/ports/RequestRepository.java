package dev.enzosantos.docbox.applications.ports;

import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequestAcknowledgedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractRequestSentPayload;
import java.util.UUID;

public interface RequestRepository {
  void create(AbstractRequest request);

  void markAsSent(UUID id, AbstractRequestSentPayload payload);

  void markAsAcknowledged(UUID id, AbstractRequestAcknowledgedPayload payload);
}
