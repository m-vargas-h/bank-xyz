# Bank XYZ - Spring Batch

Migración de procesos legacy del Banco XYZ utilizando Spring Batch.

## Descripción

Este proyecto implementa la migración de procesos batch del sistema legacy del Banco XYZ, reemplazando scripts COBOL/Shell por soluciones Java modernas utilizando Spring Batch.

Spring Batch es un framework de procesamiento por lotes que permite ejecutar operaciones sobre grandes volúmenes de datos de forma estructurada, confiable y repetible. En este caso, se utiliza para procesar archivos CSV con datos bancarios y persistirlos en una base de datos MySQL, replicando la lógica de negocio que anteriormente ejecutaban los sistemas legacy.

Cada proceso batch está implementado como un Job independiente, lo que permite ejecutarlos de forma aislada según la necesidad operacional del banco.

---

## Arquitectura

El proyecto sigue la arquitectura estándar de Spring Batch, donde cada Job está compuesto por uno o más Steps que encadenan tres componentes principales:

- **ItemReader:** Lee los datos desde un archivo CSV línea por línea y los convierte en objetos Java. Se utiliza `FlatFileItemReader` configurado con el delimitador y los nombres de columna correspondientes a cada archivo.

- **ItemProcessor:** Recibe cada objeto del Reader, aplica la lógica de negocio (validaciones, transformaciones o cálculos) y retorna el objeto procesado. Si retorna `null`, el item es descartado sin interrumpir el Job.

- **ItemWriter:** Recibe los objetos procesados y los persiste en MySQL mediante `JdbcBatchItemWriter`, ejecutando el INSERT correspondiente a cada tabla.

El flujo de datos es el siguiente:
```
CSV → ItemReader → ItemProcessor → ItemWriter → MySQL
                                       ↓
                               ResumenWriter → MySQL (tablas de resumen)
```

Los Jobs de transacciones diarias y estados de cuenta anuales incorporan un segundo Step que consolida los datos procesados en tablas de resumen independientes.

Los Jobs se lanzan de forma manual a través de endpoints REST expuestos por el `JobController`, lo que permite ejecutar cada proceso de forma independiente y controlada.

---

## Jobs implementados

### dailyTransactionReportJob

Procesa el archivo `transacciones.csv` en dos Steps encadenados. El Processor valida que el monto de cada transacción sea mayor a cero, descartando aquellas con monto negativo o igual a cero como anomalías. Las transacciones válidas se persisten en `transaccion_reporte`. Un segundo Step genera un resumen consolidado con el total de transacciones procesadas, monto total y cantidad de anomalías detectadas, persistido en `transaccion_resumen`.

| Componente | Clase | Descripción |
|------------|-------|-------------|
| Reader | `FlatFileItemReader` | Lee `transacciones.csv` |
| Processor | `TransaccionProcessor` | Descarta montos negativos o cero |
| Writer (Step 1) | `JdbcBatchItemWriter` | Inserta en `transaccion_reporte` |
| Writer (Step 2) | `TransaccionResumenWriter` | Consolida resumen en `transaccion_resumen` |

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

Procesa el archivo `cuentas_anuales.csv` en dos Steps encadenados. El Processor descarta movimientos con monto cero o negativo. Los movimientos válidos se persisten en `cuenta_anual_reporte`. Un segundo Step consolida los movimientos agrupados por `cuenta_id`, calculando total de movimientos y monto acumulado por cuenta, persistido en `cuenta_anual_resumen` para uso en auditorías.

| Componente | Clase | Descripción |
|------------|-------|-------------|
| Reader | `FlatFileItemReader` | Lee `cuentas_anuales.csv` |
| Processor | `CuentaAnualProcessor` | Descarta movimientos sin monto válido |
| Writer (Step 1) | `JdbcBatchItemWriter` | Inserta en `cuenta_anual_reporte` |
| Writer (Step 2) | `CuentaAnualResumenWriter` | Consolida resumen en `cuenta_anual_resumen` |

---

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
│       ├── evidencia_app.png
│       ├── evidencia_app1.png
│       ├── evidencia_docker.png
│       ├── evidencia_job1_db.png
│       ├── evidencia_job1_ejecucion.png
│       ├── evidencia_job1_ejecucion1.png
│       ├── evidencia_job2_db.png
│       ├── evidencia_job2_ejecucion.png
│       ├── evidencia_job2_ejecucion1.png
│       ├── evidencia_job3_db.png
│       ├── evidencia_job3_ejecucion.png
│       ├── evidencia_job3_ejecucion1.png
│       ├── evidencia_test.png
│       └── evidencia_test1.png
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
│   │   │               ├── listener
│   │   │               │   ├── BankSkipListener.java
│   │   │               │   └── JobCompletionListener.java
│   │   │               ├── model
│   │   │               │   ├── CuentaAnual.java
│   │   │               │   ├── CuentaAnualResumen.java
│   │   │               │   ├── Interes.java
│   │   │               │   ├── Transaccion.java
│   │   │               │   └── TransaccionResumen.java
│   │   │               ├── policy
│   │   │               │   └── BankSkipPolicy.java
│   │   │               ├── processor
│   │   │               │   ├── CuentaAnualProcessor.java
│   │   │               │   ├── InteresProcessor.java
│   │   │               │   └── TransaccionProcessor.java
│   │   │               ├── writer
│   │   │               │   ├── CuentaAnualResumenWriter.java
│   │   │               │   └── TransaccionResumenWriter.java
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

---

## Optimización y resiliencia (Semana 2)

En la segunda semana se incorporaron mejoras orientadas a optimizar el rendimiento y garantizar la estabilidad del sistema ante errores.

### Procesamiento paralelo

Cada Job cuenta con un `ThreadPoolTaskExecutor` configurado con 3 hilos, permitiendo procesar múltiples chunks simultáneamente. Para garantizar thread-safety en la lectura, el `FlatFileItemReader` de cada Job está envuelto en un `SynchronizedItemStreamReader`.

| Parámetro | Valor |
|---|---|
| Chunk size | 5 |
| Hilos por Job | 3 |
| Prefijos de hilo | `daily-batch-*`, `monthly-batch-*`, `annual-batch-*` |

### Tolerancia a fallos

Se implementó una `BankSkipPolicy` personalizada que permite omitir registros problemáticos sin detener el Job, con un límite de 10 omisiones por ejecución. Complementariamente, cada Step cuenta con una `RetryPolicy` que reintenta hasta 3 veces ante errores transitorios.

| Política | Clase | Límite |
|---|---|---|
| Skip | `BankSkipPolicy` | 10 omisiones |
| Retry | `SimpleRetryPolicy` | 3 reintentos |

### Listeners

| Listener | Clase | Función |
|---|---|---|
| Job | `JobCompletionListener` | Loguea nombre, estado y duración de cada Job |
| Skip | `BankSkipListener` | Loguea registros omitidos en lectura, proceso y escritura |

---

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
docker exec -it bank-xyz-mysql mysql -uroot -proot bank_xyz -e "SELECT * FROM transaccion_resumen;"
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
docker exec -it bank-xyz-mysql mysql -uroot -proot bank_xyz -e "SELECT * FROM cuenta_anual_resumen;"
```

![Job 3 base de datos](docs/images/evidencia_job3_db.png)

---
