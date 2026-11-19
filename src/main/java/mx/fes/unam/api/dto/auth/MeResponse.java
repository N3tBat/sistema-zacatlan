package mx.fes.unam.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeResponse {
    private String username;
    private String nombre;
    private String apellidos;
    private String email;
    private String rol;
    private Boolean activo;
    private Long bloqueId;
}
