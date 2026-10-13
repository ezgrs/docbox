package dev.enzosantos.docbox.applications.ports;

import dev.enzosantos.docbox.domain.payloads.AbstractRequestAcknowledgedPayload;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestCompletedPayload;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestEvaluatedPayload;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestReceivedPayload;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestRejectedPayload;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestReturnedPayload;
import dev.enzosantos.docbox.domain.payloads.AbstractRequestSentPayload;
import dev.enzosantos.docbox.domain.requests.AbstractRequest;
import java.util.Optional;
import java.util.UUID;

public interface RequestRepository {
  void create(AbstractRequest request);

  Optional<AbstractRequest> read(UUID id);

  void delete(UUID id);

  void markAsSent(UUID id, AbstractRequestSentPayload payload);

  void markAsAcknowledged(UUID id, AbstractRequestAcknowledgedPayload payload);

  void markAsAccepted(UUID id, AbstractRequestEvaluatedPayload payload);

  void markAsRejected(UUID id, AbstractRequestRejectedPayload payload);

  void markAsReturned(UUID id, AbstractRequestReturnedPayload payload);

  void markAsReceived(UUID id, AbstractRequestReceivedPayload payload);

  void markAsCompleted(UUID id, AbstractRequestCompletedPayload payload);
}
