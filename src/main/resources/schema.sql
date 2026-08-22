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

CREATE TABLE IF NOT EXISTS cuenta_anual_reporte (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id     INT,
    fecha         VARCHAR(20),
    transaccion   VARCHAR(20),
    monto         DOUBLE,
    descripcion   VARCHAR(255),
    estado        VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS transaccion_resumen (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    fecha_reporte   VARCHAR(20),
    total_procesadas INT,
    monto_total     DOUBLE,
    total_anomalias INT
);

CREATE TABLE IF NOT EXISTS cuenta_anual_resumen (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id       INT,
    total_movimientos INT,
    monto_total     DOUBLE,
    fecha_reporte   VARCHAR(20)
);