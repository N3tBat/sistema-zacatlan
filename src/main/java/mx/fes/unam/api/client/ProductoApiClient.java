package mx.fes.unam.api.client;

import lombok.RequiredArgsConstructor;
import mx.fes.unam.api.dto.producto.ProductoRequest;
import mx.fes.unam.api.dto.producto.ProductoResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductoApiClient {

    private final RestClient restClient;

    public List<ProductoResponse> listar() {
        return restClient.get()
                .uri("/api/productos")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductoResponse>>() {});
    }

    public List<ProductoResponse> listarActivos() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/productos").queryParam("activos", "true").build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductoResponse>>() {});
    }

    public ProductoResponse obtener(Long id) {
        return restClient.get()
                .uri("/api/productos/{id}", id)
                .retrieve()
                .body(ProductoResponse.class);
    }

    public ProductoResponse crear(ProductoRequest request) {
        return restClient.post()
                .uri("/api/productos")
                .body(request)
                .retrieve()
                .body(ProductoResponse.class);
    }

    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        return restClient.put()
                .uri("/api/productos/{id}", id)
                .body(request)
                .retrieve()
                .body(ProductoResponse.class);
    }

    public void eliminar(Long id) {
        restClient.delete()
                .uri("/api/productos/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}
