package mx.fes.unam.api.dto.vendedor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendedorRequest {
    private String nombre;
    private String apellidos;
    private String telefono;
    private String email;
    private String username;
    private String password;
    private Long bloqueId;
}
