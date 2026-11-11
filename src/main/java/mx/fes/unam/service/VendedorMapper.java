package mx.fes.unam.service;

import mx.fes.unam.model.entities.bloque.BloqueEntity;
import mx.fes.unam.model.entities.vendedor.VendedorDTO;
import mx.fes.unam.model.entities.vendedor.VendedorEntity;
import org.springframework.stereotype.Component;

@Component
public class VendedorMapper {

    public VendedorDTO toDto(VendedorEntity entity) {
        if (entity == null) {
            return null;
        }
        VendedorDTO dto = VendedorDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .apellidos(entity.getApellidos())
                .telefono(entity.getTelefono())
                .email(entity.getEmail())
                .username(entity.getUsername())
                .rol(entity.getRol())
                .activo(entity.getActivo())
                .fechaAlta(entity.getFechaAlta())
                .fechaBaja(entity.getFechaBaja())
                .build();

        if (entity.getBloque() != null) {
            dto.setBloqueId(entity.getBloque().getId());
            dto.setBloqueNombre(entity.getBloque().getNombre());
        }

        return dto;
    }

    public VendedorEntity toEntity(VendedorDTO dto, BloqueEntity bloque) {
        if (dto == null) {
            return null;
        }
        return VendedorEntity.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .apellidos(dto.getApellidos())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .rol(dto.getRol())
                .activo(dto.getActivo())
                .fechaAlta(dto.getFechaAlta())
                .fechaBaja(dto.getFechaBaja())
                .bloque(bloque)
                .build();
    }
}
