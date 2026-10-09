CREATE TABLE IF NOT EXISTS transaccion_saga (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id INT NOT NULL,
    tipo      VARCHAR(20) NOT NULL,
    monto     INT NOT NULL,
    fecha     VARCHAR(20),
    estado    VARCHAR(20) NOT NULL,
    motivo    VARCHAR(255)
);
