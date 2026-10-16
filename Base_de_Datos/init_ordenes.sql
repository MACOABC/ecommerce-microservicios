-- Ejecutar sobre ecommerce_ordenes.
BEGIN;
CREATE TABLE ordenes (
 id UUID PRIMARY KEY,
 cliente VARCHAR(100) NOT NULL,
 email VARCHAR(160) NOT NULL,
 direccion VARCHAR(240) NOT NULL,
 aprobar_pago BOOLEAN NOT NULL,
 estado VARCHAR(16) NOT NULL DEFAULT 'CREADA' CHECK (estado IN ('CREADA','PAGADA','RECHAZADA')),
 total NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (total >= 0),
 creada TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE orden_items (
 orden_id UUID NOT NULL REFERENCES ordenes(id),
 producto_id BIGINT NOT NULL,
 cantidad INTEGER NOT NULL CHECK (cantidad BETWEEN 1 AND 999),
 nombre VARCHAR(120),
 precio NUMERIC(12,2),
 PRIMARY KEY (orden_id, producto_id)
);
-- Ejemplo histórico independiente del inventario inicial.
INSERT INTO ordenes(id,cliente,email,direccion,aprobar_pago,estado,total)
 VALUES ('00000000-0000-0000-0000-000000000001','Cliente de ejemplo','demo@example.com','Av. Ejemplo 123, Lima',TRUE,'PAGADA',89.00);
INSERT INTO orden_items VALUES ('00000000-0000-0000-0000-000000000001',3,1,'Lámpara de escritorio',89.00);
COMMIT;
