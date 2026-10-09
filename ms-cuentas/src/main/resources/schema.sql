CREATE TABLE IF NOT EXISTS cuenta (
    cuenta_id INT PRIMARY KEY,
    titular   VARCHAR(100) NOT NULL,
    saldo     BIGINT NOT NULL,
    estado    VARCHAR(20) NOT NULL DEFAULT 'ACTIVA'
);

CREATE TABLE IF NOT EXISTS movimiento_cuenta (
    transaccion_id INT PRIMARY KEY,
    cuenta_id      INT NOT NULL,
    tipo           VARCHAR(20) NOT NULL,
    monto          INT NOT NULL,
    resultado      VARCHAR(20) NOT NULL,
    motivo         VARCHAR(255),
    fecha_proceso  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);