# Bank XYZ - Spring Batch

Migración de procesos legacy del Banco XYZ utilizando Spring Batch.

## Descripción

Este proyecto implementa la migración de procesos batch del sistema legacy del Banco XYZ, reemplazando scripts COBOL/Shell por soluciones Java modernas utilizando Spring Batch.

Spring Batch es un framework de procesamiento por lotes que permite ejecutar operaciones sobre grandes volúmenes de datos de forma estructurada, confiable y repetible. En este caso, se utiliza para procesar archivos CSV con datos bancarios y persistirlos en una base de datos MySQL, replicando la lógica de negocio que anteriormente ejecutaban los sistemas legacy.

Cada proceso batch está implementado como un Job independiente, lo que permite ejecutarlos de forma aislada según la necesidad operacional del banco.

---

## Arquitectura

El proyecto sigue la arquitectura estándar de Spring Batch, donde cada Job está compuesto por un Step que encadena tres componentes principales:

- **ItemReader:** Lee los datos desde un archivo CSV línea por línea y los convierte en objetos Java. Se utiliza `FlatFileItemReader` configurado con el delimitador y los nombres de columna correspondientes a cada archivo.

- **ItemProcessor:** Recibe cada objeto del Reader, aplica la lógica de negocio (validaciones, transformaciones o cálculos) y retorna el objeto procesado. Si retorna `null`, el item es descartado sin interrumpir el Job.

- **ItemWriter:** Recibe los objetos procesados y los persiste en MySQL mediante `JdbcBatchItemWriter`, ejecutando el INSERT correspondiente a cada tabla.

El flujo de datos es el siguiente:
```
CSV → ItemReader → ItemProcessor → ItemWriter → MySQL
```

Los Jobs se lanzan de forma manual a través de endpoints REST expuestos por el `JobController`, lo que permite ejecutar cada proceso de forma independiente y controlada.

---

## Jobs implementados

### dailyTransactionReportJob

Procesa el archivo `transacciones.csv` que contiene los movimientos diarios del banco. El Processor valida que el monto de cada transacción sea mayor a cero, descartando aquellas con monto negativo o igual a cero como anomalías. Las transacciones válidas se persisten en la tabla `transaccion_reporte` con estado `PROCESADO`.

| Componente | Clase | Descripción |
|------------|-------|-------------|
| Reader | `FlatFileItemReader` | Lee `transacciones.csv` |
| Processor | `TransaccionProcessor` | Descarta montos negativos o cero |
| Writer | `JdbcBatchItemWriter` | Inserta en `transaccion_reporte` |

---

### monthlyInterestJob

Procesa el archivo `intereses.csv` que contiene las cuentas bancarias con sus saldos y tipos. El Processor aplica una tasa de interés según el tipo de cuenta (`ahorro` 3%, `prestamo` 5%, `hipoteca` 4%), calcula el interés generado y el saldo final. Las cuentas con saldo cero o tipo desconocido son descartadas. Los resultados se persisten en la tabla `interes_reporte`.

| Componente | Clase | Descripción |
|------------|-------|-------------|
| Reader | `FlatFileItemReader` | Lee `intereses.csv` |
| Processor | `InteresProcessor` | Calcula interés según tipo de cuenta |
| Writer | `JdbcBatchItemWriter` | Inserta en `interes_reporte` |

---

### annualStatementJob

Procesa el archivo `cuentas_anuales.csv` que contiene los movimientos anuales por cuenta para fines de auditoría. El Processor descarta movimientos con monto cero o negativo. Los movimientos válidos se persisten en la tabla `cuenta_anual_reporte` con estado `PROCESADO`.

| Componente | Clase | Descripción |
|------------|-------|-------------|
| Reader | `FlatFileItemReader` | Lee `cuentas_anuales.csv` |
| Processor | `CuentaAnualProcessor` | Descarta movimientos sin monto válido |
| Writer | `JdbcBatchItemWriter` | Inserta en `cuenta_anual_reporte` |

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
├── docs
│   └── images
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

En Spring Batch, cuando un `ItemProcessor` retorna `null` para un item, ese item es descartado silenciosamente sin interrumpir la ejecución del Job. Esto permite manejar datos inválidos o inconsistentes de forma controlada, continuando el procesamiento del resto de los registros.

En este proyecto cada Processor implementa esta estrategia para los siguientes casos:

| Job | Caso | Acción |
|-----|------|--------|
| `dailyTransactionReportJob` | Monto negativo o cero | Item descartado |
| `monthlyInterestJob` | Saldo cero o negativo | Item descartado |
| `monthlyInterestJob` | Tipo de cuenta desconocido | Item descartado |
| `annualStatementJob` | Monto cero o negativo | Item descartado |

Cada vez que un item es descartado, el Processor imprime un mensaje en consola identificando el registro afectado, lo que permite trazabilidad sobre los datos que no fueron procesados.

## Tests

El proyecto incluye un test de contexto (`contextLoads`) que verifica que la aplicación levanta correctamente con todos sus beans y configuraciones. 

Para evitar dependencia de una base de datos MySQL en ejecución durante los tests, se utiliza H2 como base de datos en memoria. Esto se logra mediante `@TestPropertySource` que sobreescribe las propiedades de conexión definidas en `application.properties` únicamente durante la ejecución de los tests, sin modificar la configuración de producción.

Esta estrategia permite ejecutar los tests en cualquier entorno sin necesidad de tener Docker o MySQL instalado, lo que facilita la integración continua y la verificación rápida del proyecto.

```bash
./mvnw clean test
```

![Test inicio](docs/images/evidencia_test.png)
![Test resultado](docs/images/evidencia_test1.png)

---

## Evidencia de ejecución

### 1. Levantar base de datos

```bash
docker-compose up -d
```

![Docker Compose](docs/images/evidencia_docker.png)

---

### 2. Iniciar aplicación

```bash
./mvnw spring-boot:run
```

![App iniciada](docs/images/evidencia_app.png)
![App iniciada consola](docs/images/evidencia_app1.png)

---

### 3. Job 1 - Reporte de transacciones diarias

```bash
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/jobs/daily-transaction"
```

![Job 1 ejecución](docs/images/evidencia_job1_ejecucion.png)
![Job 1 consola](docs/images/evidencia_job1_ejecucion1.png)

```bash
docker exec -it bank-xyz-mysql mysql -uroot -proot bank_xyz -e "SELECT * FROM transaccion_reporte;"
```

![Job 1 base de datos](docs/images/evidencia_job1_db.png)

---

### 4. Job 2 - Cálculo de intereses mensuales

```bash
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/jobs/monthly-interest"
```

![Job 2 ejecución](docs/images/evidencia_job2_ejecucion.png)
![Job 2 consola](docs/images/evidencia_job2_ejecucion1.png)

```bash
docker exec -it bank-xyz-mysql mysql -uroot -proot bank_xyz -e "SELECT * FROM interes_reporte;"
```

![Job 2 base de datos](docs/images/evidencia_job2_db.png)

---

### 5. Job 3 - Estado de cuentas anuales

```bash
Invoke-RestMethod -Method POST -Uri "http://localhost:8080/jobs/annual-statement"
```

![Job 3 ejecución](docs/images/evidencia_job3_ejecucion.png)
![Job 3 consola](docs/images/evidencia_job3_ejecucion1.png)

```bash
docker exec -it bank-xyz-mysql mysql -uroot -proot bank_xyz -e "SELECT * FROM cuenta_anual_reporte;"
```

![Job 3 base de datos](docs/images/evidencia_job3_db.png)

---

