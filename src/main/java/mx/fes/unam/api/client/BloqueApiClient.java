package mx.fes.unam.api.client;

import lombok.RequiredArgsConstructor;
import mx.fes.unam.api.dto.bloque.BloqueRequest;
import mx.fes.unam.api.dto.bloque.BloqueResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BloqueApiClient {

    private final RestClient restClient;

    public List<BloqueResponse> listar() {
        return restClient.get()
                .uri("/api/bloques")
                .retrieve()
                .body(new ParameterizedTypeReference<List<BloqueResponse>>() {});
    }

    public List<BloqueResponse> listarActivos() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/bloques").queryParam("activos", "true").build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<BloqueResponse>>() {});
    }

    public BloqueResponse obtener(Long id) {
        return restClient.get()
                .uri("/api/bloques/{id}", id)
                .retrieve()
                .body(BloqueResponse.class);
    }

    public BloqueResponse crear(BloqueRequest request) {
        return restClient.post()
                .uri("/api/bloques")
                .body(request)
                .retrieve()
                .body(BloqueResponse.class);
    }

    public BloqueResponse actualizar(Long id, BloqueRequest request) {
        return restClient.put()
                .uri("/api/bloques/{id}", id)
                .body(request)
                .retrieve()
                .body(BloqueResponse.class);
    }

    public void eliminar(Long id) {
        restClient.delete()
                .uri("/api/bloques/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}
