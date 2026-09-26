# Bank XYZ — Tolerancia a Fallos y Arquitectura de Eventos con Kafka

Extensión de la arquitectura de microservicios bancarios incorporando comunicación asíncrona mediante Apache Kafka bajo el Patrón Saga con Coreografía, integración con base de datos MySQL poblada por Spring Batch, y tolerancia a fallos con Resilience4j.

---

## Descripción

El proyecto implementa una arquitectura orientada a eventos para el Banco XYZ, compuesta por diez servicios independientes:

- **config-server**: configuración centralizada para todos los microservicios.
- **eureka-server**: service discovery donde se registran los microservicios.
- **auth-server**: servidor de autorización OAuth2 que emite tokens JWT.
- **bank-xyz-batch**: jobs de Spring Batch que procesan y persisten datos bancarios en MySQL.
- **mysql**: base de datos relacional que almacena los datos procesados por el batch.
- **zookeeper**: coordinador requerido por Kafka.
- **kafka**: broker de mensajería para la comunicación asíncrona entre microservicios.
- **ms-cuentas**: microservicio de cuentas bancarias — consumer y producer Kafka.
- **ms-transacciones**: microservicio de transacciones — producer principal Kafka.
- **ms-clientes**: microservicio de clientes — consumer final Kafka.

---

## Arquitectura de eventos — Patrón Saga con Coreografía

El sistema implementa el Patrón Saga con Coreografía sobre Apache Kafka. Cada microservicio actúa de forma autónoma, publicando y consumiendo eventos sin un orquestador central.

### Tópicos Kafka

| Tópico | Productor | Consumidor(es) |
|---|---|---|
| `transaccion-registrada` | ms-transacciones | ms-cuentas, ms-clientes |
| `cuenta-actualizada` | ms-cuentas | ms-clientes |
| `notificacion-cliente` | ms-clientes | Sistema externo / log |
| `transaccion-rechazada` | ms-cuentas (fallo) | ms-transacciones (compensación) |

### Flujo feliz (happy path)

```
Cliente → POST /api/transacciones → ms-transacciones
  → publica transaccion-registrada
    → ms-cuentas consume → verifica saldo OK → publica cuenta-actualizada
    → ms-clientes consume transaccion-registrada
    → ms-clientes consume cuenta-actualizada → registra historial
```

### Flujo compensatorio (Saga rollback)

```
ms-cuentas detecta saldo insuficiente
  → publica transaccion-rechazada
    → ms-transacciones consume → actualiza estado → FALLIDA
```

---

## Arquitectura general

```
[Config Server :8888]
        ↑ configuración centralizada
[Eureka Server :8761]
        ↑ service discovery
[Auth Server :9000]
        ↑ tokens JWT OAuth2
[MySQL :3306] ← [bank-xyz-batch :8080]
        ↑ datos reales
[ms-cuentas :8081] [ms-transacciones :8082] [ms-clientes :8083]
        ↕ eventos asincrónicos
[Kafka :9092] ← [Zookeeper :2181]
```

---

## Estructura del repositorio

```

```

---

## Tecnologías

- Java 21
- Spring Boot 3.5.0
- Spring Cloud 2025.0.0
- Spring Kafka 3.3.6 / Apache Kafka 3.9.1
- Spring Security OAuth2 Authorization Server
- Spring Security OAuth2 Resource Server
- Resilience4j (Circuit Breaker, Retry, TimeLimiter, RateLimiter)
- Netflix Eureka
- Spring Cloud Config
- MySQL 8.0
- Spring Batch
- Docker / Docker Compose

---

## Requisitos previos

- Java 21
- Maven
- Docker Desktop
- Postman

---

## Configuración y ejecución

### Compilar todos los proyectos

```bash
cd config-server
./mvnw clean package -DskipTests
cd ../eureka-server
./mvnw clean package -DskipTests
cd ../auth-server
./mvnw clean package -DskipTests
cd ../bank-xyz-batch
./mvnw clean package -DskipTests
cd ../ms-cuentas
./mvnw clean package -DskipTests
cd ../ms-transacciones
./mvnw clean package -DskipTests
cd ../ms-clientes
./mvnw clean package -DskipTests
cd ..
```

### Levantar todos los servicios

```bash
docker-compose up --build
```

### Poblar la base de datos (ejecutar tras el primer levantamiento)

```bash
POST http://localhost:8080/jobs/daily-transaction
POST http://localhost:8080/jobs/monthly-interest
POST http://localhost:8080/jobs/annual-statement
```

### Servicios disponibles

| Servicio | URL |
|---|---|
| Config Server | `http://localhost:8888` |
| Eureka Server | `http://localhost:8761` |
| Auth Server | `http://localhost:9000` |
| Batch | `http://localhost:8080` |
| ms-cuentas | `http://localhost:8081` |
| ms-transacciones | `http://localhost:8082` |
| ms-clientes | `http://localhost:8083` |
| Kafka | `localhost:9092` |
| MySQL | `localhost:3306` |

---

## Autenticación OAuth2

Todas las rutas protegidas requieren un token JWT obtenido desde el auth-server.

**Obtener token:**

| Campo | Valor |
|---|---|
| URL | `POST http://localhost:9000/oauth2/token` |
| Auth | Basic Auth |
| client_id | `bank-xyz-client` |
| client_secret | `secret123` |
| grant_type | `client_credentials` |
| scope | `transacciones.write` / `transacciones.read` / `cuentas.read` / `clientes.read` |

---

## Endpoints disponibles

### bank-xyz-batch (puerto 8080)

| Endpoint | Método | Descripción |
|---|---|---|
| `/jobs/daily-transaction` | POST | Ejecuta job de transacciones diarias |
| `/jobs/monthly-interest` | POST | Ejecuta job de intereses mensuales |
| `/jobs/annual-statement` | POST | Ejecuta job de estados de cuenta anuales |

### ms-transacciones (puerto 8082)

| Endpoint | Método | Acceso | Descripción |
|---|---|---|---|
| `/api/transacciones` | GET | Público | Lista transacciones desde BD |
| `/api/transacciones` | POST | JWT `transacciones.write` | Registra transacción y publica evento Kafka |
| `/api/transacciones/info` | GET | Público | Info del microservicio |
| `/api/transacciones/resilience` | GET | JWT `transacciones.read` | Circuit Breaker + Retry + TimeLimiter |
| `/api/transacciones/ratelimit` | GET | JWT `transacciones.read` | Rate Limiter |

### ms-cuentas (puerto 8081)

| Endpoint | Método | Acceso | Descripción |
|---|---|---|---|
| `/api/cuentas` | GET | Público | Lista cuentas anuales desde BD |
| `/api/cuentas/intereses` | GET | Público | Lista intereses desde BD |
| `/api/cuentas/resumen` | GET | Público | Resumen de cuentas desde BD |
| `/api/cuentas/resilience` | GET | JWT `cuentas.read` | Circuit Breaker + Retry + TimeLimiter |
| `/api/cuentas/ratelimit` | GET | JWT `cuentas.read` | Rate Limiter |

### ms-clientes (puerto 8083)

| Endpoint | Método | Acceso | Descripción |
|---|---|---|---|
| `/api/clientes` | GET | Público | Lista clientes desde BD |
| `/api/clientes/intereses` | GET | Público | Lista intereses por cliente desde BD |
| `/api/clientes/resilience` | GET | JWT `clientes.read` | Circuit Breaker + Retry + TimeLimiter |
| `/api/clientes/ratelimit` | GET | JWT `clientes.read` | Rate Limiter |

---

## Resilience4j — Patrones implementados

| Patrón | Configuración |
|---|---|
| Circuit Breaker | Se abre con 50% de fallos en ventana de 10 llamadas; espera 10s en estado OPEN |
| Retry | 3 intentos con backoff exponencial (x2) cada 1s |
| Time Limiter | Timeout de 2 segundos por llamada |
| Rate Limiter | Máximo 5 llamadas cada 10 segundos |

---

## Evidencia de ejecución

### 1. Contenedores levantados con Docker Compose

![Docker Compose](docs/images/evidencia_docker_s7.png)

---

### 2. Eureka — 3 microservicios registrados

![Eureka dashboard](docs/images/evidencia_eureka_s7.png)

---

### 3. Jobs del batch ejecutados (BD poblada)

![Job transacciones diarias](docs/images/evidencia_job_transacciones_s7.png)
![Job intereses mensuales](docs/images/evidencia_job_intereses_s7.png)
![Job estados de cuenta](docs/images/evidencia_job_cuentas_s7.png)

---

### 4. Datos reales desde BD — endpoints GET

![ms-transacciones GET](docs/images/evidencia_transacciones_get_s7.png)
![ms-cuentas GET](docs/images/evidencia_cuentas_get_s7.png)
![ms-clientes GET](docs/images/evidencia_clientes_get_s7.png)

---

### 5. Obtención de token JWT

![Token JWT](docs/images/evidencia_token_s7.png)

---

### 6. Flujo Kafka — happy path

![POST transacción](docs/images/evidencia_kafka_post_s7.png)
![Logs ms-cuentas consume y publica](docs/images/evidencia_kafka_cuentas_log_s7.png)
![Logs ms-clientes consume](docs/images/evidencia_kafka_clientes_log_s7.png)

---

### 7. Flujo Kafka — compensación Saga

![POST transacción rechazada](docs/images/evidencia_kafka_rechazo_post_s7.png)
![Logs ms-cuentas saldo insuficiente](docs/images/evidencia_kafka_rechazo_cuentas_s7.png)
![Logs ms-transacciones compensación](docs/images/evidencia_kafka_compensacion_s7.png)
