package dev.enzosantos.docbox.domain.payloads;

public interface RequestRejectedPayload extends RequestEvaluatedPayload {
  String getRejectionReason();
}
