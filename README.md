# Microservicio CRUD de Productos (HU-01)

API REST para administrar productos con persistencia en MongoDB. Permite crear, listar, consultar, actualizar y eliminar productos; valida los datos de entrada y devuelve errores de negocio y validación en un formato JSON uniforme.

## Stack tecnológico

- **Java 21**
- **Spring Boot 3.4.5**: Spring Web, Spring Data MongoDB y Bean Validation
- **MongoDB**: almacenamiento documental
- **JUnit 5 y Mockito**: pruebas unitarias de la capa de servicio
- **Spring Boot Test y MockMvc**: pruebas de integración de la API
- **Flapdoodle Embedded MongoDB**: MongoDB local para las pruebas de integración
- **JaCoCo 0.8.12**: reporte y verificación de cobertura
- **SonarQube Community LTS**: análisis estático
- **Docker Compose**: ejecución local de MongoDB y SonarQube
- **Maven Wrapper**: ejecución de Maven sin instalarlo globalmente

## Arquitectura y estructura

El código sigue una separación por responsabilidades: el controlador expone HTTP, el servicio implementa las reglas de negocio, el repositorio accede a MongoDB, el DTO valida las solicitudes y el manejador global normaliza errores.

```text
.
├── docker-compose.yml
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
└── src/
    ├── main/
    │   ├── java/com/hu01/productos/
    │   │   ├── MicroserviciosHu01Application.java
    │   │   ├── controller/
    │   │   │   └── ProductoController.java
    │   │   ├── dto/
    │   │   │   └── ProductoRequest.java
    │   │   ├── exception/
    │   │   │   ├── ApiError.java
    │   │   │   ├── DatoDuplicadoException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── RecursoNoEncontradoException.java
    │   │   ├── model/
    │   │   │   └── Producto.java
    │   │   ├── repository/
    │   │   │   └── ProductoRepository.java
    │   │   └── service/
    │   │       └── ProductoService.java
    │   └── resources/
    │       └── application.yml
    └── test/
        ├── java/com/hu01/productos/
        │   ├── ProductoControllerIntegrationTest.java
        │   └── service/
        │       └── ProductoServiceTest.java
        └── resources/
            └── application.yml
```

El producto contiene `id`, `codigo`, `nombre`, `descripcion`, `precio` y `stock`. `codigo` tiene índice único en MongoDB. El DTO requiere código y nombre no vacíos, precio mayor que cero y stock igual o mayor que cero; la descripción es opcional.

## Requisitos previos

- Java 21
- Docker y Docker Compose
- Acceso a Internet la primera vez que se use Maven Wrapper, para descargar Maven y dependencias

Los comandos siguientes se ejecutan desde la raíz del repositorio.

## Puesta en marcha

### 1. Iniciar MongoDB y SonarQube

```bash
docker-compose up -d
```

Compose inicia:

| Servicio | Imagen | Puerto local |
|---|---|---:|
| MongoDB | `mongo:latest` | `27017` |
| SonarQube | `sonarqube:lts-community` | `9000` |

La aplicación se conecta a `mongodb://localhost:27017/hu01_db`, según `src/main/resources/application.yml`. SonarQube estará disponible en <http://localhost:9000>. La primera inicialización del servicio puede tardar unos minutos; revisa su estado con `docker-compose ps` y sus registros con `docker-compose logs -f sonarqube`.

### 2. Ejecutar pruebas y verificar cobertura

```bash
./mvnw clean verify
```

La suite incluye pruebas unitarias de `ProductoService` con Mockito y pruebas de integración con `@SpringBootTest` y MockMvc. Estas últimas usan MongoDB embebido y no requieren una instancia MongoDB externa. JaCoCo genera los reportes HTML y XML bajo `target/site/jacoco/`; las reglas de `pom.xml` exigen que la cobertura de líneas y ramas esté entre 80% y 90%.

Para consultar el reporte HTML, abre `target/site/jacoco/index.html`.

### 3. Ejecutar el análisis estático con SonarQube

Primero inicia SonarQube con Compose y completa la configuración inicial en <http://localhost:9000>. Crea allí un token de análisis y ejecútalo sin guardar el token en el repositorio:

```bash
./mvnw clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=TU_TOKEN \
  -Dsonar.projectKey=microservicios-hu01
```

`clean verify` genera el XML de JaCoCo antes del análisis. La propiedad `sonar.coverage.jacoco.xmlReportPaths` de `pom.xml` apunta a `target/site/jacoco/jacoco.xml`. Sustituye `TU_TOKEN` por el token local y no lo incluyas en archivos versionados.

### 4. Arrancar el microservicio

Con MongoDB en ejecución:

```bash
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`. Para detener los servicios de infraestructura:

```bash
docker-compose down
```

## API REST

Todas las rutas usan el prefijo `/api/productos`. Los errores controlados se devuelven como JSON con los campos `timestamp`, `status`, `error`, `mensaje` y `ruta`.

| Método HTTP | Ruta exacta | Descripción | Códigos HTTP |
|---|---|---|---|
| `POST` | `/api/productos` | Crea un producto. | `201 Created`, `400 Bad Request` si falla la validación, `409 Conflict` si el código ya existe. |
| `GET` | `/api/productos` | Devuelve la lista de productos. | `200 OK`. |
| `GET` | `/api/productos/{id}` | Consulta un producto por su identificador. | `200 OK`, `404 Not Found` si no existe. |
| `PUT` | `/api/productos/{id}` | Actualiza los datos del producto. | `200 OK`, `400 Bad Request` si falla la validación, `404 Not Found` si no existe, `409 Conflict` si el código pertenece a otro producto. |
| `DELETE` | `/api/productos/{id}` | Elimina un producto. | `204 No Content`, `404 Not Found` si no existe. |

### Crear o actualizar un producto

El mismo formato de solicitud se usa para `POST` y `PUT`:

```http
Content-Type: application/json
```

```json
{
  "codigo": "P-100",
  "nombre": "Teclado mecánico",
  "descripcion": "Teclado con distribución latinoamericana",
  "precio": 49.90,
  "stock": 10
}
```

Una creación exitosa (`201 Created`) devuelve el producto persistido, incluido el `id` asignado por MongoDB:

```json
{
  "id": "673a1b2c3d4e5f6789012345",
  "codigo": "P-100",
  "nombre": "Teclado mecánico",
  "descripcion": "Teclado con distribución latinoamericana",
  "precio": 49.90,
  "stock": 10
}
```

### Formato de error

`GlobalExceptionHandler`, mediante `@RestControllerAdvice`, normaliza errores de validación (`400`), recurso no encontrado (`404`) y código duplicado (`409`). Ejemplo de validación:

```json
{
  "timestamp": "2026-11-13T23:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "nombre: no debe estar vacío; precio: debe ser mayor que 0",
  "ruta": "/api/productos"
}
```

El texto de `mensaje` depende de los campos y restricciones que fallen en la solicitud.

## Autor


**Joaquín Alberto López Martínez**

Ingeniería en Desarrollo de Software Multiplataforma
