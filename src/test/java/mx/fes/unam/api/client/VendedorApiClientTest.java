package mx.fes.unam.api.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class VendedorApiClientTest {

    private MockRestServiceServer server;
    private VendedorApiClient vendedorApiClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder
                .baseUrl("http://localhost:8080")
                .build();
        vendedorApiClient = new VendedorApiClient(restClient);
    }

    @Test
    void desactivarDebeAceptarRespuesta204SinCuerpo() {
        server.expect(requestTo("http://localhost:8080/api/vendedores/1/desactivar"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        assertDoesNotThrow(() -> vendedorApiClient.desactivar(1L));

        server.verify();
    }

    @Test
    void reactivarDebeAceptarRespuesta204SinCuerpo() {
        server.expect(requestTo("http://localhost:8080/api/vendedores/2/reactivar"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        assertDoesNotThrow(() -> vendedorApiClient.reactivar(2L));

        server.verify();
    }
}
