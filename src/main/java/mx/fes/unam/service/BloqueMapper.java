package mx.fes.unam.service;

import mx.fes.unam.model.entities.bloque.BloqueDTO;
import mx.fes.unam.model.entities.bloque.BloqueEntity;
import org.springframework.stereotype.Component;

@Component
public class BloqueMapper {

    public BloqueDTO toDto(BloqueEntity entity) {
        if (entity == null) {
            return null;
        }
        return BloqueDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .activo(entity.getActivo())
                .build();
    }

    public BloqueEntity toEntity(BloqueDTO dto) {
        if (dto == null) {
            return null;
        }
        return BloqueEntity.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
    }
}
