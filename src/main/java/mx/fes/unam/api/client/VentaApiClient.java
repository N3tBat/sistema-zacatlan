package mx.fes.unam.api.client;

import lombok.RequiredArgsConstructor;
import mx.fes.unam.api.dto.venta.VentaRequest;
import mx.fes.unam.api.dto.venta.VentaResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VentaApiClient {

    private final RestClient restClient;

    public List<VentaResponse> listar() {
        return restClient.get()
                .uri("/api/ventas")
                .retrieve()
                .body(new ParameterizedTypeReference<List<VentaResponse>>() {});
    }

    public VentaResponse obtener(Long id) {
        return restClient.get()
                .uri("/api/ventas/{id}", id)
                .retrieve()
                .body(VentaResponse.class);
    }

    public VentaResponse crear(VentaRequest request) {
        return restClient.post()
                .uri("/api/ventas")
                .body(request)
                .retrieve()
                .body(VentaResponse.class);
    }

    public VentaResponse actualizar(Long id, VentaRequest request) {
        return restClient.put()
                .uri("/api/ventas/{id}", id)
                .body(request)
                .retrieve()
                .body(VentaResponse.class);
    }
}
