package com.raulrobinson.infrastructure.http.error;

public final class ExternalClientException extends GenericApiClientException {
    private final int status;
    private final String responseBody;

    public ExternalClientException(String operation, int status, String responseBody) {
        super(operation, "El backend respondió HTTP " + status, null);
        this.status = status;
        this.responseBody = responseBody;
    }

    public int status() { return status; }
    public String responseBody() { return responseBody; }
}
