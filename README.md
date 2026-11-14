# Microservicios HU-01: gestión de productos

API REST Spring Boot 3 para crear, consultar, actualizar y eliminar productos en MongoDB.

## Requisitos

- Java 21
- Docker con Docker Compose

## Infraestructura local

Desde la raíz del proyecto:

```bash
docker compose up -d
```

MongoDB queda disponible en `localhost:27017` y SonarQube en `http://localhost:9000`. La base de datos de la API es `hu01_db`.

## Compilar y probar

```bash
./mvnw clean verify
```

El comando ejecuta las pruebas unitarias y de integración y valida la cobertura JaCoCo. Los reportes HTML y XML se generan en `target/site/jacoco/`; líneas y ramas deben tener entre 80% y 90%.

Para ejecutar solamente las pruebas:

```bash
./mvnw test
```

## Análisis de SonarQube

Tras iniciar SonarQube y completar su configuración inicial, ejecutar:

```bash
./mvnw sonar:sonar
```

El análisis consume el reporte XML indicado por `sonar.coverage.jacoco.xmlReportPaths` en `pom.xml`. Para configurar el servidor local con Maven, se puede proporcionar `-Dsonar.host.url=http://localhost:9000` y un token mediante `-Dsonar.token=...`.

## Endpoints

| Método | Ruta | Acción |
|---|---|---|
| POST | `/api/productos` | Crear producto (201) |
| GET | `/api/productos` | Listar productos |
| GET | `/api/productos/{id}` | Consultar producto |
| PUT | `/api/productos/{id}` | Actualizar producto |
| DELETE | `/api/productos/{id}` | Eliminar producto (204) |
