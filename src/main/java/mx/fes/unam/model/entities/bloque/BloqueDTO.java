package mx.fes.unam.model.entities.bloque;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloqueDTO {

    private Long id;

    @NotBlank(message = "El nombre del bloque es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String nombre;

    @Size(max = 250, message = "La descripción no debe exceder 250 caracteres")
    private String descripcion;

    @Builder.Default
    private Boolean activo = true;
}
