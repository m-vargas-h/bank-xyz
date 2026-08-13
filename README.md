# Bank XYZ - Spring Batch

Migración de procesos legacy del Banco XYZ utilizando Spring Batch.

## Descripción

Este proyecto implementa tres jobs de Spring Batch para modernizar los procesos
batch del sistema legacy del Banco XYZ, reemplazando scripts COBOL/Shell por
soluciones Java modernas con Spring Boot 3.3.5 y Spring Batch 5.1.2.

## Jobs implementados

| Job | Endpoint | Descripción |
|-----|----------|-------------|
| `dailyTransactionReportJob` | `POST /jobs/daily-transaction` | Procesa transacciones diarias, detecta anomalías y genera resumen |
| `monthlyInterestJob` | `POST /jobs/monthly-interest` | Aplica intereses según tipo de cuenta y actualiza saldo final |
| `annualStatementJob` | `POST /jobs/annual-statement` | Compila movimientos anuales por cuenta para auditoría |

## Tecnologías

- Java 21
- Spring Boot 3.3.5
- Spring Batch 5.1.2
- MySQL 8.0
- Docker / Docker Compose
- Lombok

## Requisitos previos

- Java 21
- Maven
- Docker Desktop

## Configuración y ejecución

### 1. Levantar la base de datos

```bash
docker-compose up -d
```

### 2. Compilar el proyecto

```bash
./mvnw clean install -DskipTests
```

### 3. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

### 4. Ejecutar los jobs

```bash
# Job 1 - Reporte de transacciones diarias
curl -X POST http://localhost:8080/jobs/daily-transaction

# Job 2 - Cálculo de intereses mensuales
curl -X POST http://localhost:8080/jobs/monthly-interest

# Job 3 - Estado de cuentas anuales
curl -X POST http://localhost:8080/jobs/annual-statement
```

## Estructura del proyecto

```
├── .mvn
│   └── wrapper
│       └── maven-wrapper.properties
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── duoc
│   │   │           └── bank_xyz
│   │   │               ├── config
│   │   │               │   ├── AnnualStatementJobConfig.java
│   │   │               │   ├── DailyTransactionJobConfig.java
│   │   │               │   └── MonthlyInterestJobConfig.java
│   │   │               ├── controller
│   │   │               │   └── JobController.java
│   │   │               ├── model
│   │   │               │   ├── CuentaAnual.java
│   │   │               │   ├── Interes.java
│   │   │               │   └── Transaccion.java
│   │   │               ├── processor
│   │   │               │   ├── CuentaAnualProcessor.java
│   │   │               │   ├── InteresProcessor.java
│   │   │               │   └── TransaccionProcessor.java
│   │   │               └── BankXyzApplication.java
│   │   └── resources
│   │       ├── static
│   │       ├── templates
│   │       ├── application.properties
│   │       ├── cuentas_anuales.csv
│   │       ├── intereses.csv
│   │       ├── schema.sql
│   │       └── transacciones.csv
│   └── test
│       └── java
│           └── com
│               └── duoc
│                   └── bank_xyz
│                       └── BankXyzApplicationTests.java
├── .gitattributes
├── .gitignore
├── README.md
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## Manejo de anomalías

| Job | Caso | Acción |
|-----|------|--------|
| `dailyTransactionReportJob` | Monto negativo o cero | Item descartado |
| `monthlyInterestJob` | Saldo cero o negativo | Item descartado |
| `monthlyInterestJob` | Tipo de cuenta desconocido | Item descartado |
| `annualStatementJob` | Monto cero o negativo | Item descartado |

## Evidencia de ejecución

*(Capturas de consola adjuntas en la entrega)*