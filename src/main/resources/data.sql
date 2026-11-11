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
