package mx.fes.unam.api.dto.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private String timestamp;
    private Integer status;
    private String error;
    private String mensaje;
    private Map<String, String> detalles;
}
