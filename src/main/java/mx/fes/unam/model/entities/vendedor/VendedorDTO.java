package mx.fes.unam.model.entities.vendedor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.fes.unam.security.RolEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendedorDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no deben exceder 100 caracteres")
    private String apellidos;

    @Pattern(regexp = "^$|^[0-9]{10,15}$", message = "El teléfono debe contener entre 10 y 15 dígitos")
    private String telefono;

    @Email(message = "El correo electrónico no tiene un formato válido")
    @Size(max = 150, message = "El correo electrónico no debe exceder 150 caracteres")
    private String email;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 50, message = "El usuario no debe exceder 50 caracteres")
    private String username;

    private RolEnum rol;

    @Builder.Default
    private Boolean activo = true;

    private LocalDateTime fechaAlta;

    private LocalDateTime fechaBaja;

    private Long bloqueId;

    private String bloqueNombre;
}
