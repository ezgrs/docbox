package dev.enzosantos.docbox.applications.ports;

import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import dev.enzosantos.docbox.domain.requests.AbstractRequestAcknowledgedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractRequestCompletedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractRequestEvaluatedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractRequestRejectedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractRequestReturnedPayload;
import dev.enzosantos.docbox.domain.requests.AbstractRequestSentPayload;
import java.util.UUID;

public interface RequestRepository {
  void create(AbstractRequest request);

  void markAsSent(UUID id, AbstractRequestSentPayload payload);

  void markAsAcknowledged(UUID id, AbstractRequestAcknowledgedPayload payload);

  void markAsAccepted(UUID id, AbstractRequestEvaluatedPayload payload);

  void markAsRejected(UUID id, AbstractRequestRejectedPayload payload);

  void markAsCompleted(UUID id, AbstractRequestCompletedPayload payload);

  void markAsReturned(UUID id, AbstractRequestReturnedPayload payload);
}
