# Instrucciones de ejecución y pruebas

Cómo levantar el sistema y probar cada componente. Contexto general en [`readme.md`](README.md); despliegue de Kafka en AWS en [`despliegue.md`](despliegue.md).

## 1. Requisitos

- Docker Desktop con Docker Compose v2
- Java 21 (solo para compilar y ejecutar los tests)
- PowerShell (usar `curl.exe`, no el alias `curl`)

## 2. Preparación

```powershell
Copy-Item .env.example .env
```

El `.env` define `MYSQL_ROOT_PASSWORD` y `KAFKA_BOOTSTRAP_SERVERS`. Para usar el Kafka local, deja `KAFKA_BOOTSTRAP_SERVERS` vacío o bórralo; para usar el de AWS, ver `despliegue.md`.

Compila los módulos (los tests de integración que necesitan base de datos se omiten):

```powershell
foreach ($m in "config-server","eureka-server","auth-server","ms-cuentas","ms-transacciones","ms-clientes","bank-xyz-bff","bank-xyz-batch") {
  Push-Location $m; ./mvnw clean package -DskipTests; Pop-Location
}
```

## 3. Levantar el sistema

```powershell
docker compose up -d --build
docker compose ps
```

Espera 1 a 2 minutos a que los servicios queden `Up` o `healthy`.

| Servicio | URL |
|---|---|
| Eureka | http://localhost:8761 |
| Auth Server | http://localhost:9000 |
| Config Server | http://localhost:8888 |
| ms-cuentas · ms-transacciones · ms-clientes | :8081 · :8082 · :8083 |
| BFF (HTTPS) | https://localhost:8443 |
| Batch | http://localhost:8080 |

Verifica en Eureka que aparezcan `MS-CUENTAS`, `MS-TRANSACCIONES`, `MS-CLIENTES` y `BANK-XYZ-BFF`.

## 4. Autenticación (OAuth2)

Obtener un token y llamar a un microservicio directamente:

```powershell
$t = (curl.exe -s -u bank-xyz-client:secret123 -d "grant_type=client_credentials" `
  --data-urlencode "scope=cuentas.read transacciones.read" http://localhost:9000/oauth2/token | ConvertFrom-Json).access_token

curl.exe -H "Authorization: Bearer $t" http://localhost:8081/api/cuentas/101/saldo
curl.exe http://localhost:8081/api/cuentas/101/saldo        # sin token: 401
```

Scopes: `cuentas.read`, `transacciones.read`, `clientes.read`, `transacciones.write`.

## 5. BFF por canal

Credenciales (HTTP Basic, HTTPS con certificado autofirmado, por eso `-k`):

| Canal | Usuario | Contraseña | Rol |
|---|---|---|---|
| Web | `web_user` | `web_pass_2024` | WEB |
| Móvil | `mobile_user` | `mobile_pass_2024` | MOBILE |
| ATM | `atm_user` | `atm_pass_2024` | ATM |

El mismo recurso, con respuesta distinta por canal:

```powershell
curl.exe -k -u web_user:web_pass_2024       https://localhost:8443/web/transacciones/resumen
curl.exe -k -u mobile_user:mobile_pass_2024 https://localhost:8443/mobile/transacciones/resumen
curl.exe -k -u atm_user:atm_pass_2024       https://localhost:8443/atm/resumen
```

Cada canal solo accede a su ruta:

```powershell
curl.exe -k -u atm_user:atm_pass_2024 https://localhost:8443/web/cuentas     # 403
```

Otras rutas: `/web/cuentas`, `/web/intereses`, `/mobile/cuentas/{id}`, `/atm/saldo/{cuentaId}`, `/atm/transacciones/{cuentaId}`.

## 6. Saga: retiro desde el ATM

Crea los cuerpos de prueba (cuentas de ejemplo: 101 a 120):

```powershell
'{"cuentaId":101,"monto":5000}'    | Set-Content -Encoding ascii retiro-ok.json
'{"cuentaId":101,"monto":3000000}' | Set-Content -Encoding ascii retiro-rechazado.json
'{"cuentaId":9999,"monto":1000}'   | Set-Content -Encoding ascii retiro-inexistente.json
```

**Caso exitoso:**

```powershell
curl.exe -k -u atm_user:atm_pass_2024 https://localhost:8443/atm/saldo/101
curl.exe -k -u atm_user:atm_pass_2024 -H "Content-Type: application/json" -d "@retiro-ok.json" https://localhost:8443/atm/retiro
curl.exe -k -u atm_user:atm_pass_2024 https://localhost:8443/atm/retiro/<id>     # COMPLETADA
curl.exe -k -u atm_user:atm_pass_2024 https://localhost:8443/atm/saldo/101       # saldo menos 5000
```

La respuesta del POST es `202` con estado `PENDIENTE`. El saldo y el estado se actualizan en un par de segundos, porque la Saga es asíncrona.

**Rechazos (compensación):**

```powershell
curl.exe -k -u atm_user:atm_pass_2024 -H "Content-Type: application/json" -d "@retiro-rechazado.json" https://localhost:8443/atm/retiro
curl.exe -k -u atm_user:atm_pass_2024 https://localhost:8443/atm/retiro/<id>     # FALLIDA, "Saldo insuficiente"

curl.exe -k -u atm_user:atm_pass_2024 -H "Content-Type: application/json" -d "@retiro-inexistente.json" https://localhost:8443/atm/retiro
curl.exe -k -u atm_user:atm_pass_2024 https://localhost:8443/atm/retiro/<id>     # FALLIDA, "Cuenta inexistente o inactiva"
```

**Estado en base de datos y logs:**

```powershell
docker compose exec mysql mysql -uroot -proot bank_xyz -e "SELECT * FROM cuenta WHERE cuenta_id=101; SELECT * FROM transaccion_saga; SELECT * FROM movimiento_cuenta;"
docker compose logs ms-cuentas ms-transacciones --tail 30
```

(Si cambiaste `MYSQL_ROOT_PASSWORD`, usa esa clave en lugar de `root`.)

## 7. Procesos batch

Los tres jobs se lanzan por HTTP. `threads` y `chunkSize` son opcionales y sobrescriben los valores por defecto.

```powershell
curl.exe -X POST "http://localhost:8080/jobs/daily-transaction?threads=4&chunkSize=100"
curl.exe -X POST "http://localhost:8080/jobs/monthly-interest?threads=4&chunkSize=100"
curl.exe -X POST "http://localhost:8080/jobs/annual-statement?threads=4&chunkSize=100"
```

Cada respuesta indica job, estado (`COMPLETED`), intentos, hilos y chunk. Si una ejecución falla, el controlador la reintenta automáticamente (`batch.max-reintentos`).

Verificar resultados e idempotencia (ejecuta un job dos veces y compara conteos):

```powershell
docker compose exec mysql mysql -uroot -proot bank_xyz -e "SELECT COUNT(*) FROM transaccion_reporte; SELECT * FROM transaccion_resumen; SELECT COUNT(*) FROM cuenta_anual_resumen; SELECT COUNT(*) FROM interes_reporte;"
curl.exe -k -u web_user:web_pass_2024 https://localhost:8443/web/transacciones/resumen
docker compose logs batch --tail 40
```

## 8. Resiliencia

```powershell
docker compose stop ms-cuentas
curl.exe -k -u web_user:web_pass_2024 https://localhost:8443/web/cuentas     # 503 controlado
docker compose up -d --no-deps ms-cuentas
```

Tras unos segundos el servicio vuelve a responder. Endpoints de demostración en los microservicios (con token): `/api/cuentas/resilience`, `/api/cuentas/ratelimit`, `/api/transacciones/resilience`, `/api/transacciones/ratelimit`.

## 9. Monitoreo

```powershell
curl.exe http://localhost:8081/actuator/health
```

Los logs de cada servicio: `docker compose logs <servicio> --tail 50`.

## 10. Pruebas automáticas

```powershell
cd bank-xyz-bff; ./mvnw clean package
```

Los tests del BFF verifican roles por canal, respuestas 400/403/404 y el flujo de retiro.

## 11. Detener y limpiar

```powershell
docker compose down          # conserva los datos de MySQL
docker compose down -v       # borra también los datos
```

## 12. Kafka en AWS

Para usar el broker de la EC2 en lugar del Kafka local, sigue [`despliegue.md`](despliegue.md).

## 13. Pruebas con Postman

La batería completa está en `docs/Postman/`:

- `bank-xyz.postman_collection.json`: 40 requests con tests, agrupados por componente (autenticación, BFF web, móvil y ATM, Saga, batch, microservicios, Actuator y resiliencia).
- `bank-xyz.postman_environment.json`: environment "Banco XYZ - Local".

Pasos:

1. Importa ambos archivos en Postman y selecciona el environment **Banco XYZ - Local**.
2. Desactiva *SSL certificate verification* (Settings → General), porque el BFF usa un certificado autofirmado.
3. Ejecuta cada carpeta con *Run folder*, en este orden: autenticación, BFF Web, Móvil, ATM, Saga, Batch, Microservicios y Actuator.

La carpeta de resiliencia es manual: requiere detener `ms-cuentas` (`docker compose stop ms-cuentas`) y volver a levantarlo con `docker compose up -d --no-deps ms-cuentas`.