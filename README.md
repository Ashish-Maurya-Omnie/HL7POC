# HL7POC

Java Spring Boot POC to work with HL7 FHIR in two modes:
1. Local mode using Docker (HAPI FHIR server)
2. AWS mode using Amazon HealthLake (SigV4 signed requests)

## Tech Stack
- Java 17+
- Spring Boot 3.x
- Maven
- HAPI FHIR (R4)
- AWS SDK v2 (SigV4 signer)
- Docker Compose (for local FHIR)
- Swagger / OpenAPI (`springdoc-openapi`)

## Project Layout
- `docker/docker-compose.yml` local HAPI FHIR server
- `src/main/java/com/example/hl7poc` Spring Boot source
- `src/main/resources/application.yml` default config (LOCAL mode)
- `src/main/resources/application-aws.yml` AWS profile overrides

## Prerequisites
- Java 17+
- Maven 3.9+
- Docker Desktop or Docker Engine
- AWS credentials configured locally for AWS mode (`AWS_PROFILE` or env credentials)

## Configuration
### Default (`application.yml`)
- `server.port=8081`
- `fhir.client.mode=LOCAL`
- `fhir.client.local-base-url=http://localhost:8080/fhir`
- `fhir.client.aws-base-url=` (empty by default)
- `fhir.client.aws-region=us-east-1`
- `fhir.client.connect-timeout=10s`
- `fhir.client.read-timeout=30s`

### AWS Profile (`application-aws.yml`)
Uses environment variables:
- `FHIR_CLIENT_AWS_BASE_URL`
- `FHIR_CLIENT_AWS_REGION` (default `us-east-1`)

Example HealthLake base URL format:
- `https://healthlake.<region>.amazonaws.com/datastore/<datastore-id>/r4`

## Run Steps
### 1. Start local HL7 FHIR server in Docker
From project root:

```bash
cd docker
docker compose up -d
```

Verify local FHIR is running:

```bash
curl http://localhost:8080/fhir/metadata
```

### 2. Start Spring Boot in local mode
From project root:

```bash
mvn spring-boot:run
```

App URL:
- `http://localhost:8081`

Health check:

```bash
curl http://localhost:8081/api/v1/fhir/health
```

Swagger UI:

```bash
open http://localhost:8081/swagger-ui.html
```

OpenAPI JSON:

```bash
curl http://localhost:8081/v3/api-docs
```

### 3. Run in AWS HealthLake mode
From project root:

```bash
export FHIR_CLIENT_AWS_BASE_URL="https://healthlake.<region>.amazonaws.com/datastore/<datastore-id>/r4"
export FHIR_CLIENT_AWS_REGION="us-east-1"
export AWS_PROFILE="your-profile"

mvn spring-boot:run -Dspring-boot.run.profiles=aws
```

## API Endpoints
Base path: `/api/v1/fhir`

- `GET /health` app + active mode info
- `GET /metadata` fetch FHIR CapabilityStatement
- `GET /patients/{id}` read Patient by ID
- `GET /patients/search?family=...&given=...` search Patient
- `POST /patients` create Patient

Swagger/OpenAPI endpoints:
- `GET /swagger-ui.html`
- `GET /v3/api-docs`

## Example Requests
### Metadata
```bash
curl http://localhost:8081/api/v1/fhir/metadata
```

### Search Patient
```bash
curl "http://localhost:8081/api/v1/fhir/patients/search?family=Smith&given=John"
```

### Create Patient
```bash
curl -X POST http://localhost:8081/api/v1/fhir/patients \
  -H "Content-Type: application/json" \
  -d '{
    "identifiers": [
      {"system": "http://hospital.example.org/mrn", "value": "MRN-10001"}
    ],
    "names": [
      {"family": "Doe", "given": ["John"]}
    ],
    "gender": "male",
    "birthDate": "1990-01-01",
    "telecoms": [
      {"system": "phone", "value": "+1-555-123-4567", "use": "mobile"}
    ],
    "addresses": [
      {
        "line": ["123 Main St"],
        "city": "Seattle",
        "state": "WA",
        "postalCode": "98101",
        "country": "US"
      }
    ]
  }'
```

## Stop Local Docker FHIR
```bash
cd docker
docker compose down
```

## Troubleshooting
- `mvn: command not found`:
  Install Maven or use Maven Wrapper if you add it.
- Port conflict on `8080` or `8081`:
  Change ports in `docker/docker-compose.yml` and/or `application.yml`.
- AWS 403/401 in AWS mode:
  Check `AWS_PROFILE`, IAM permissions, region, and HealthLake datastore URL.
- Empty AWS base URL:
  Ensure `FHIR_CLIENT_AWS_BASE_URL` is exported before running AWS profile.

## Current Scope
- FHIR R4
- Patient read/search/create
- Metadata fetch
- Local Docker-first development, then HealthLake integration
