package mx.fes.unam.model.entities.bloque;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bloques")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloqueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 250)
    private String descripcion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}
