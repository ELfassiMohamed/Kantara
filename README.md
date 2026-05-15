# Kantara (Railway)

Hosted deployment branch for Kantara: a document preprocessor that extracts PDF, DOCX, PPTX, and CSV into LLM-friendly JSON, Markdown, or HTML.

## Deploy on Railway

1. Create a Railway service from this repository.
2. Set the **deployment branch** to **`railway`** (not `main`).
3. Railway reads build and start settings from [`railway.json`](railway.json):
   - **Build:** `mvn -B clean package -DskipTests`
   - **Start:** `java -jar target/kantara.jar`
   - **Health check:** `GET /api/health`
4. Railway injects **`PORT`**; the app binds to that port automatically.
5. Push to `railway` to trigger deploys: `git push origin railway`

### Optional service variables

| Variable | Purpose |
|----------|---------|
| `JAVA_OPTS` | JVM tuning, e.g. `-XX:MaxRAMPercentage=75.0` for PDF/Office workloads |
| `NIXPACKS_JDK_VERSION` | Set to `21` if the build does not pick Java 21 automatically |

### Security note

There is **no authentication** on `/api/process`. If the service has a public URL, anyone can upload files and use CPU/memory. Use Railway networking or add auth if that is not acceptable.

## Features

- **Embedded Web UI:** SPA bundled in the JAR.
- **Formats:** PDF, DOCX, PPTX, CSV.
- **Output:** JSON, Markdown, HTML.
- **Token optimization** and **LLM chunking** with token estimates.
- **Batch uploads** via the web UI (50MB per file).

On Railway, the **Exit** control is hidden and `/api/shutdown` is disabled (`RAILWAY_ENVIRONMENT` is set).

## Local run (this branch)

Requires Java 21 and Maven 3.8+.

```bash
mvn -B clean package
java -jar target/kantara.jar
```

Opens `http://localhost:7070` in your browser when `PORT` is not set.

## `main` branch

Desktop releases and GitHub Actions live on **`main`**. This branch is intentionally slimmed for Railway only; merge `main` into `railway` when you need application updates without bringing back `.github/workflows/`.
