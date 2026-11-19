package mx.fes.unam.api.support;

import mx.fes.unam.api.dto.error.ApiError;
import org.springframework.http.client.ClientHttpResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;

public final class BackendErrorHandler {

    private BackendErrorHandler() {
    }

    public static ApiError readApiError(ClientHttpResponse response, ObjectMapper objectMapper) {
        try (InputStream body = response.getBody()) {
            byte[] bytes = body.readAllBytes();
            if (bytes.length == 0) {
                return fallback(response);
            }
            ApiError error = objectMapper.readValue(bytes, ApiError.class);
            if (error != null && error.getStatus() == null) {
                error.setStatus(response.getStatusCode().value());
            }
            return error;
        } catch (Exception ex) {
            return fallback(response);
        }
    }

    private static ApiError fallback(ClientHttpResponse response) {
        ApiError error = new ApiError();
        try {
            error.setStatus(response.getStatusCode().value());
        } catch (Exception ignored) {
            // sin status disponible
        }
        error.setMensaje("Error al consumir el backend");
        return error;
    }
}
