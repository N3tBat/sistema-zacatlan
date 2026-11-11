package mx.fes.unam.service;

import mx.fes.unam.model.entities.venta.VentaDTO;
import mx.fes.unam.model.entities.venta.VentaEntity;
import org.springframework.stereotype.Component;

@Component
public class VentaMapper {

    public VentaDTO toDto(VentaEntity entity) {
        if (entity == null) {
            return null;
        }
        VentaDTO dto = VentaDTO.builder()
                .id(entity.getId())
                .fecha(entity.getFecha())
                .cantidad(entity.getCantidad())
                .precioAplicado(entity.getPrecioAplicado())
                .total(entity.getTotal())
                .vendedorNombre(entity.getVendedorNombre())
                .adminId(entity.getAdminId())
                .adminUsername(entity.getAdminUsername())
                .movimiento(entity.getMovimiento())
                .fechaModificacion(entity.getFechaModificacion())
                .build();

        if (entity.getVendedor() != null) {
            dto.setVendedorId(entity.getVendedor().getId());
            if (entity.getVendedor().getBloque() != null) {
                dto.setBloqueNombre(entity.getVendedor().getBloque().getNombre());
            }
        }
        if (entity.getProducto() != null) {
            dto.setProductoId(entity.getProducto().getId());
            dto.setProductoNombre(entity.getProducto().getNombre());
        }

        return dto;
    }
}
