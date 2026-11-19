package mx.fes.unam.api.config;

import jakarta.servlet.http.HttpSession;
import mx.fes.unam.api.client.BackendApiException;
import mx.fes.unam.api.dto.error.ApiError;
import mx.fes.unam.api.support.ApiSessionKeys;
import mx.fes.unam.api.support.BackendErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

/**
 * Configuración del cliente HTTP hacia el backend REST.
 * Añade Authorization: Bearer <token> desde HttpSession para todas las llamadas excepto POST /api/auth/login.
 * Convierte respuestas 4xx/5xx en BackendApiException con ApiError.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient backendRestClient(RestClient.Builder builder,
                                        @Value("${api.base-url}") String baseUrl,
                                        ObjectMapper objectMapper) {
        return builder
                .baseUrl(baseUrl)
                .requestInterceptor(bearerTokenInterceptor())
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    ApiError apiError = BackendErrorHandler.readApiError(response, objectMapper);
                    throw new BackendApiException(response.getStatusCode(), apiError);
                })
                .build();
    }

    private ClientHttpRequestInterceptor bearerTokenInterceptor() {
        return (request, body, execution) -> {
            String path = request.getURI().getPath();
            if (path != null && path.endsWith("/api/auth/login")) {
                return execution.execute(request, body);
            }

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpSession session = attrs.getRequest().getSession(false);
                if (session != null) {
                    Object token = session.getAttribute(ApiSessionKeys.ACCESS_TOKEN);
                    if (token instanceof String tokenStr && !tokenStr.isBlank()) {
                        request.getHeaders().setBearerAuth(tokenStr);
                    }
                }
            }
            return execution.execute(request, body);
        };
    }
}
