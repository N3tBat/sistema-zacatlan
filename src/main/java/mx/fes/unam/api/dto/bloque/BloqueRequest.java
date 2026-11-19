package mx.fes.unam.api.dto.bloque;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloqueRequest {
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
