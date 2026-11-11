-- ==============================================================================
-- SISTEMA ZACATLÁN - DATOS SEMILLA (data.sql)
-- ==============================================================================

-- SECCIÓN PERSONA A: bloques
INSERT IGNORE INTO bloques (id, nombre, descripcion, activo) VALUES
(1, 'Zona Centro', 'Área comercial del primer cuadro del centro histórico de Zacatlán', 1),
(2, 'Mercado Municipal', 'Locales y pasillos dentro del Mercado Revolución', 1),
(3, 'Sección Manzanas y Sidra', 'Corredor tradicional de productores artesanales de manzana, sidra y licores', 1),
(4, 'Tianguis del Domingo', 'Puestos semifijos y comerciantes de la explanada dominical', 1),
(5, 'Mirador y Reloj Floral', 'Zona turística del paseo de la barranca y el reloj floral', 1),
(6, 'Sección Temporal', 'Módulos temporales para ferias y eventos especiales (sin asignaciones activas)', 0);

-- SECCIÓN PERSONA A: administradores y vendedores
-- Contraseña admin123 (BCrypt): $2a$10$O7IAl9Xelw.bnTHkRC5joOPeZIgFGf2Dluv07WeVNSBT4ZsmVMoc6
-- Contraseña vendedor123 (BCrypt): $2a$10$tFwa5DQMkxUXr4DWJScJsuIq2Ozb1V3zAVz/JY2YpUMs9kcdGAFrq

INSERT IGNORE INTO vendedor (id, nombre, apellidos, telefono, email, username, password, rol, activo, fecha_alta, fecha_baja, bloque_id) VALUES
-- Administradores Municipales (bloque NULL)
(1, 'Administrador', 'Municipal', '7979750001', 'admin@zacatlan.gob.mx', 'admin', '$2a$10$O7IAl9Xelw.bnTHkRC5joOPeZIgFGf2Dluv07WeVNSBT4ZsmVMoc6', 'ADMIN', 1, '2026-08-01 08:00:00', NULL, NULL),
(2, 'Dirección de', 'Comercio', '7979750002', 'comercio@zacatlan.gob.mx', 'municipal', '$2a$10$O7IAl9Xelw.bnTHkRC5joOPeZIgFGf2Dluv07WeVNSBT4ZsmVMoc6', 'ADMIN', 1, '2026-08-01 08:00:00', NULL, NULL),

-- Vendedores activos (vendedor1 a vendedor8)
(3, 'Carlos', 'Hernández López', '7971012341', 'vendedor1@ejemplo.com', 'vendedor1', '$2a$10$tFwa5DQMkxUXr4DWJScJsuIq2Ozb1V3zAVz/JY2YpUMs9kcdGAFrq', 'VENDEDOR', 1, '2026-08-10 09:30:00', NULL, 1),
(4, 'María', 'González Trejo', '7971012342', 'vendedor2@ejemplo.com', 'vendedor2', '$2a$10$tFwa5DQMkxUXr4DWJScJsuIq2Ozb1V3zAVz/JY2YpUMs9kcdGAFrq', 'VENDEDOR', 1, '2026-08-18 10:15:00', NULL, 1),
(5, 'José', 'Martínez Rojas', '7971012343', 'vendedor3@ejemplo.com', 'vendedor3', '$2a$10$tFwa5DQMkxUXr4DWJScJsuIq2Ozb1V3zAVz/JY2YpUMs9kcdGAFrq', 'VENDEDOR', 1, '2026-08-25 11:00:00', NULL, 2),
(6, 'Guadalupe', 'Rodríguez Flores', '7971012344', 'vendedor4@ejemplo.com', 'vendedor4', '$2a$10$tFwa5DQMkxUXr4DWJScJsuIq2Ozb1V3zAVz/JY2YpUMs9kcdGAFrq', 'VENDEDOR', 1, '2026-09-02 08:45:00', NULL, 2),
(7, 'Juan', 'Pérez Morales', '7971012345', 'vendedor5@ejemplo.com', 'vendedor5', '$2a$10$tFwa5DQMkxUXr4DWJScJsuIq2Ozb1V3zAVz/JY2YpUMs9kcdGAFrq', 'VENDEDOR', 1, '2026-09-12 12:20:00', NULL, 3),
(8, 'Rosa', 'Sánchez Díaz', '7971012346', 'vendedor6@ejemplo.com', 'vendedor6', '$2a$10$tFwa5DQMkxUXr4DWJScJsuIq2Ozb1V3zAVz/JY2YpUMs9kcdGAFrq', 'VENDEDOR', 1, '2026-09-20 14:10:00', NULL, 3),
(9, 'Miguel', 'Ramírez Castillo', '7971012347', 'vendedor7@ejemplo.com', 'vendedor7', '$2a$10$tFwa5DQMkxUXr4DWJScJsuIq2Ozb1V3zAVz/JY2YpUMs9kcdGAFrq', 'VENDEDOR', 1, '2026-10-05 09:00:00', NULL, 4),
(10, 'Ana', 'Gómez Aguilar', '7971012348', 'vendedor8@ejemplo.com', 'vendedor8', '$2a$10$tFwa5DQMkxUXr4DWJScJsuIq2Ozb1V3zAVz/JY2YpUMs9kcdGAFrq', 'VENDEDOR', 1, '2026-10-14 16:30:00', NULL, 4),

-- Vendedores inactivos (vendedor9 y vendedor10)
(11, 'Fernando', 'Ortiz Vargas', '7971012349', 'vendedor9@ejemplo.com', 'vendedor9', '$2a$10$tFwa5DQMkxUXr4DWJScJsuIq2Ozb1V3zAVz/JY2YpUMs9kcdGAFrq', 'VENDEDOR', 0, '2026-10-18 10:00:00', '2026-11-04 15:30:00', 5),
(12, 'Patricia', 'Torres Mendoza', '7971012350', 'vendedor10@ejemplo.com', 'vendedor10', '$2a$10$tFwa5DQMkxUXr4DWJScJsuIq2Ozb1V3zAVz/JY2YpUMs9kcdGAFrq', 'VENDEDOR', 0, '2026-10-25 11:15:00', '2026-11-08 17:00:00', 5);

-- SECCIÓN PERSONA B: productos y ventas (agregar debajo de esta línea)

-- Productos del municipio: mezcla de forma de venta BLOQUE y UNIDAD
INSERT IGNORE INTO producto (id, nombre, descripcion, forma_venta, precio_unidad, precio_bloque, unidades_por_bloque, activo) VALUES
(1, 'Manzana Royale', 'Caja de manzana royale de huerta local', 'BLOQUE', NULL, 450.00, 20, 1),
(2, 'Sidra Natural', 'Caja de sidra natural artesanal', 'BLOQUE', NULL, 600.00, 12, 1),
(3, 'Nuez Limpia', 'Costal de nuez limpia de la región', 'BLOQUE', NULL, 850.00, 25, 1),
(4, 'Pan de Huevo', 'Pieza de pan dulce tradicional', 'UNIDAD', 15.00, NULL, NULL, 1),
(5, 'Tamales de Rajas', 'Tamal de rajas con queso por pieza', 'UNIDAD', 25.00, NULL, NULL, 1),
(6, 'Café de Altura', 'Bolsa de 250 g de café tostado', 'UNIDAD', 90.00, NULL, NULL, 1),
(7, 'Mermelada de Manzana', 'Frasco de mermelada artesanal de 500 g', 'UNIDAD', 70.00, NULL, NULL, 1),
(8, 'Queso Fresco', 'Pieza de queso fresco de la región', 'UNIDAD', 120.00, NULL, NULL, 1),
(9, 'Vino de Frutas', 'Caja con 6 botellas de vino de frutas', 'BLOQUE', NULL, 720.00, 6, 1),
(10, 'Ate de Manzana', 'Caja con 10 barras de ate tradicional', 'BLOQUE', NULL, 350.00, 10, 1);

-- Ventas:
--  * NOW() garantiza ventas "de hoy" en cada arranque (para el reporte diario)
--  * DATE_SUB(...) simula ventas de días anteriores (para probar el filtro de fecha)
--  * vendedor_id NULL = vendedor ya eliminado (el snapshot vendedor_nombre preserva su histórico)
--  * las ventas 9 y 10 son de vendedores INACTIVOS (fechas previas a su fecha de baja)
--  * las ventas 13 y 14 ya traen la bitácora del admin (movimiento) para demostrar la auditoría
INSERT IGNORE INTO venta (id, fecha, cantidad, precio_aplicado, total, vendedor_id, vendedor_nombre, producto_id, admin_id, admin_username, movimiento, fecha_modificacion) VALUES
(1, NOW(), 2, 450.00, 900.00, 3, 'Carlos Hernández López', 1, NULL, NULL, NULL, NULL),
(2, NOW(), 3, 25.00, 75.00, 5, 'José Martínez Rojas', 5, NULL, NULL, NULL, NULL),
(3, NOW(), 1, 600.00, 600.00, 7, 'Juan Pérez Morales', 2, NULL, NULL, NULL, NULL),
(4, NOW(), 12, 15.00, 180.00, 4, 'María González Trejo', 4, NULL, NULL, NULL, NULL),
(5, DATE_SUB(NOW(), INTERVAL 1 DAY), 2, 850.00, 1700.00, 6, 'Guadalupe Rodríguez Flores', 3, NULL, NULL, NULL, NULL),
(6, DATE_SUB(NOW(), INTERVAL 1 DAY), 10, 90.00, 900.00, 8, 'Rosa Sánchez Díaz', 6, NULL, NULL, NULL, NULL),
(7, DATE_SUB(NOW(), INTERVAL 2 DAY), 5, 120.00, 600.00, 9, 'Miguel Ramírez Castillo', 8, NULL, NULL, NULL, NULL),
(8, DATE_SUB(NOW(), INTERVAL 3 DAY), 1, 350.00, 350.00, 10, 'Ana Gómez Aguilar', 10, NULL, NULL, NULL, NULL),
(9, DATE_SUB(NOW(), INTERVAL 10 DAY), 4, 70.00, 280.00, 11, 'Fernando Ortiz Vargas', 7, NULL, NULL, NULL, NULL),
(10, DATE_SUB(NOW(), INTERVAL 12 DAY), 2, 720.00, 1440.00, 12, 'Patricia Torres Mendoza', 9, NULL, NULL, NULL, NULL),
(11, DATE_SUB(NOW(), INTERVAL 15 DAY), 6, 15.00, 90.00, NULL, 'Pedro Vargas Luna', 4, NULL, NULL, NULL, NULL),
(12, DATE_SUB(NOW(), INTERVAL 18 DAY), 3, 450.00, 1350.00, NULL, 'Lucía Ferrer Manzano', 1, NULL, NULL, NULL, NULL),
(13, DATE_SUB(NOW(), INTERVAL 5 DAY), 3, 70.00, 210.00, 3, 'Carlos Hernández López', 7, 1, 'admin', 'ADMIN 1 (admin) | MODIFICÓ VENTA 13 | cantidad: 2 → 3; precioAplicado: 80.00 → 70.00; total: 160.00 → 210.00', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(14, DATE_SUB(NOW(), INTERVAL 6 DAY), 6, 720.00, 4320.00, 5, 'José Martínez Rojas', 9, 2, 'municipal', 'ADMIN 2 (municipal) | MODIFICÓ VENTA 14 | producto: Ate de Manzana → Vino de Frutas; cantidad: 4 → 6; total: 1400.00 → 4320.00', DATE_SUB(NOW(), INTERVAL 6 DAY));
