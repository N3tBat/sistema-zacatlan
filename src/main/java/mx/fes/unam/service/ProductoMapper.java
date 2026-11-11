package mx.fes.unam.service;

import mx.fes.unam.model.entities.producto.ProductoDTO;
import mx.fes.unam.model.entities.producto.ProductoEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public ProductoDTO toDto(ProductoEntity entity) {
        if (entity == null) {
            return null;
        }
        return ProductoDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .formaVenta(entity.getFormaVenta())
                .precioUnidad(entity.getPrecioUnidad())
                .precioBloque(entity.getPrecioBloque())
                .unidadesPorBloque(entity.getUnidadesPorBloque())
                .activo(entity.getActivo())
                .build();
    }

    public ProductoEntity toEntity(ProductoDTO dto) {
        if (dto == null) {
            return null;
        }
        return ProductoEntity.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .formaVenta(dto.getFormaVenta())
                .precioUnidad(dto.getPrecioUnidad())
                .precioBloque(dto.getPrecioBloque())
                .unidadesPorBloque(dto.getUnidadesPorBloque())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
    }
}
