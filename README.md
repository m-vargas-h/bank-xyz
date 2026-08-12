# Bank XYZ - Spring Batch

Migración de procesos legacy del Banco XYZ utilizando Spring Batch.

## Descripción

Este proyecto implementa tres jobs de Spring Batch para modernizar los procesos
batch del sistema legacy del Banco XYZ, reemplazando scripts COBOL/Shell por
soluciones Java modernas con Spring Boot 4.1.0 y Spring Batch.

## Jobs implementados

| Job | Descripción |
|-----|-------------|
| `dailyTransactionReportJob` | Procesa transacciones diarias, detecta anomalías y genera resumen |
| `monthlyInterestJob` | Aplica intereses según tipo de cuenta y actualiza saldo final |
| `annualStatementJob` | Compila movimientos anuales por cuenta para auditoría |

## Tecnologías

- Java 21
- Spring Boot 4.1.0
- Spring Batch
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
│   │   │               ├── model
│   │   │               │   ├── CuentaAnual.java
│   │   │               │   ├── Interes.java
│   │   │               │   └── Transaccion.java
│   │   │               └── BankXyzApplication.java
│   │   └── resources
│   │       ├── static
│   │       ├── templates
│   │       ├── application.properties
│   │       ├── cuentas_anuales.csv
│   │       ├── intereses.csv
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

## Evidencia de ejecución

*(Se adjuntará en entregas posteriores)*

