package mx.fes.unam.api.client;

import mx.fes.unam.api.dto.error.ApiError;
import org.springframework.http.HttpStatusCode;

public class BackendApiException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final ApiError apiError;

    public BackendApiException(HttpStatusCode statusCode, ApiError apiError) {
        super(apiError != null && apiError.getMensaje() != null
                ? apiError.getMensaje()
                : "Error al consumir el backend (status " + statusCode.value() + ")");
        this.statusCode = statusCode;
        this.apiError = apiError;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public int getStatus() {
        return statusCode.value();
    }

    public ApiError getApiError() {
        return apiError;
    }
}
