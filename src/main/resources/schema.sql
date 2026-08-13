CREATE TABLE IF NOT EXISTS transaccion_reporte (
    id            INT PRIMARY KEY,
    fecha         VARCHAR(20),
    monto         DOUBLE,
    tipo          VARCHAR(20),
    estado        VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS interes_reporte (
    cuenta_id     INT PRIMARY KEY,
    nombre        VARCHAR(100),
    saldo         DOUBLE,
    tipo          VARCHAR(20),
    saldo_final   DOUBLE,
    interes       DOUBLE
);

