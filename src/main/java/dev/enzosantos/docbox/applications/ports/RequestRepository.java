package dev.enzosantos.docbox.applications.ports;

import dev.enzosantos.docbox.domain.payloads.RequestAcknowledgedPayload;
import dev.enzosantos.docbox.domain.payloads.RequestCompletedPayload;
import dev.enzosantos.docbox.domain.payloads.RequestEvaluatedPayload;
import dev.enzosantos.docbox.domain.payloads.RequestReceivedPayload;
import dev.enzosantos.docbox.domain.payloads.RequestRejectedPayload;
import dev.enzosantos.docbox.domain.payloads.RequestReturnedPayload;
import dev.enzosantos.docbox.domain.payloads.RequestSentPayload;
import dev.enzosantos.docbox.domain.payloads.RequestUpdatedPayload;
import dev.enzosantos.docbox.domain.requests.Request;
import java.util.Optional;
import java.util.UUID;

public interface RequestRepository {
  void create(Request request);

  Optional<Request> read(UUID id);

  void update(UUID id, RequestUpdatedPayload payload);

  void delete(UUID id);

  void markAsNew(UUID id);

  void markAsSent(UUID id, RequestSentPayload payload);

  void markAsAcknowledged(UUID id, RequestAcknowledgedPayload payload);

  void markAsAccepted(UUID id, RequestEvaluatedPayload payload);

  void markAsRejected(UUID id, RequestRejectedPayload payload);

  void markAsReturned(UUID id, RequestReturnedPayload payload);

  void markAsReceived(UUID id, RequestReceivedPayload payload);

  void markAsCompleted(UUID id, RequestCompletedPayload payload);
}
