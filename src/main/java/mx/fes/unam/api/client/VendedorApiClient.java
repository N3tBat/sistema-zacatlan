package mx.fes.unam.api.client;

import lombok.RequiredArgsConstructor;
import mx.fes.unam.api.dto.vendedor.VendedorRequest;
import mx.fes.unam.api.dto.vendedor.VendedorResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VendedorApiClient {

    private final RestClient restClient;

    public List<VendedorResponse> listar() {
        return restClient.get()
                .uri("/api/vendedores")
                .retrieve()
                .body(new ParameterizedTypeReference<List<VendedorResponse>>() {});
    }

    public VendedorResponse obtener(Long id) {
        return restClient.get()
                .uri("/api/vendedores/{id}", id)
                .retrieve()
                .body(VendedorResponse.class);
    }

    public VendedorResponse crear(VendedorRequest request) {
        return restClient.post()
                .uri("/api/vendedores")
                .body(request)
                .retrieve()
                .body(VendedorResponse.class);
    }

    public VendedorResponse actualizar(Long id, VendedorRequest request) {
        return restClient.put()
                .uri("/api/vendedores/{id}", id)
                .body(request)
                .retrieve()
                .body(VendedorResponse.class);
    }

    public void desactivar(Long id) {
        restClient.patch()
                .uri("/api/vendedores/{id}/desactivar", id)
                .retrieve()
                .toBodilessEntity();
    }

    public void reactivar(Long id) {
        restClient.patch()
                .uri("/api/vendedores/{id}/reactivar", id)
                .retrieve()
                .toBodilessEntity();
    }

    public void eliminar(Long id) {
        restClient.delete()
                .uri("/api/vendedores/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}
