package mx.fes.unam.api.dto.vendedor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendedorResponse {
    private Long id;
    private Long bloqueId;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String email;
    private String username;
    private String rol;
    private String bloqueNombre;
    private Boolean activo;
    private LocalDateTime fechaAlta;
    private LocalDateTime fechaBaja;
}
