CREATE TABLE IF NOT EXISTS transaccion_reporte (
    id            INT PRIMARY KEY,
    fecha         VARCHAR(20),
    monto         DOUBLE,
    tipo          VARCHAR(20),
    estado        VARCHAR(20)
);