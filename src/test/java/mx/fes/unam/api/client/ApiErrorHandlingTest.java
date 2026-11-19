package mx.fes.unam.api.client;

import mx.fes.unam.api.dto.bloque.BloqueRequest;
import mx.fes.unam.api.dto.error.ApiError;
import mx.fes.unam.api.support.BackendErrorHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class ApiErrorHandlingTest {

    private MockRestServiceServer server;
    private BloqueApiClient bloqueApiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        JsonMapper objectMapper = JsonMapper.builder().build();

        RestClient restClient = builder
                .baseUrl("http://localhost:8080")
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    ApiError apiError = BackendErrorHandler.readApiError(response, objectMapper);
                    throw new BackendApiException(response.getStatusCode(), apiError);
                })
                .build();

        bloqueApiClient = new BloqueApiClient(restClient);
    }

    @Test
    void debeParsearError400ConDetalles() {
        String body = """
                {
                  "timestamp": "2026-11-19T12:34:56.789Z",
                  "status": 400,
                  "error": "Bad Request",
                  "mensaje": "La petición contiene datos inválidos",
                  "detalles": {
                    "nombre": "El nombre es obligatorio",
                    "descripcion": "La descripción es obligatoria"
                  }
                }
                """;

        server.expect(requestTo("http://localhost:8080/api/bloques"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));

        BloqueRequest request = new BloqueRequest("", "", true);

        BackendApiException ex = assertThrows(BackendApiException.class, () -> bloqueApiClient.crear(request));

        assertEquals(400, ex.getStatus());
        assertNotNull(ex.getApiError());
        assertEquals("La petición contiene datos inválidos", ex.getApiError().getMensaje());
        assertNotNull(ex.getApiError().getDetalles());
        assertEquals("El nombre es obligatorio", ex.getApiError().getDetalles().get("nombre"));
        assertEquals("La descripción es obligatoria", ex.getApiError().getDetalles().get("descripcion"));

        server.verify();
    }
}
