package dev.enzosantos.docbox.domain.requests;

public interface AbstractRequestRejectedPayload extends AbstractRequestEvaluatedPayload {
  String getRejectionReason();
}
