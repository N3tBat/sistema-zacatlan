package mx.fes.unam.api.client;

import lombok.RequiredArgsConstructor;
import mx.fes.unam.api.dto.reporte.ReporteResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ReporteApiClient {

    private final RestClient restClient;

    public ReporteResponse diario(String fechaInicio, String fechaFin, String fecha) {
        return restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/reportes/diario");
                    if (StringUtils.hasText(fechaInicio)) {
                        builder.queryParam("fechaInicio", fechaInicio);
                    }
                    if (StringUtils.hasText(fechaFin)) {
                        builder.queryParam("fechaFin", fechaFin);
                    }
                    if (StringUtils.hasText(fecha)) {
                        builder.queryParam("fecha", fecha);
                    }
                    return builder.build();
                })
                .retrieve()
                .body(ReporteResponse.class);
    }

    public ResponseEntity<byte[]> diarioPdf(String fechaInicio, String fechaFin, String fecha) {
        return restClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/reportes/diario/pdf");
                    if (StringUtils.hasText(fechaInicio)) {
                        builder.queryParam("fechaInicio", fechaInicio);
                    }
                    if (StringUtils.hasText(fechaFin)) {
                        builder.queryParam("fechaFin", fechaFin);
                    }
                    if (StringUtils.hasText(fecha)) {
                        builder.queryParam("fecha", fecha);
                    }
                    return builder.build();
                })
                .retrieve()
                .toEntity(byte[].class);
    }
}
