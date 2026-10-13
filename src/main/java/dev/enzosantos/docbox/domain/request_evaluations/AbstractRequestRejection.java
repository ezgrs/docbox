package dev.enzosantos.docbox.domain.request_evaluations;

public interface AbstractRequestRejection extends AbstractRequestEvaluation {
  String getRejectionReason();
}
