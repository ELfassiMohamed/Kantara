# Kantara (Railway)

Hosted deployment branch for Kantara: a document preprocessor that extracts PDF, DOCX, PPTX, and CSV into LLM-friendly JSON, Markdown, or HTML.


### Security note

There is **no authentication** on `/api/process`. If the service has a public URL, anyone can upload files and use CPU/memory. Use Railway networking or add auth if that is not acceptable.

## Features

- **Embedded Web UI:** SPA bundled in the JAR.
- **Formats:** PDF, DOCX, PPTX, CSV.
- **Output:** JSON, Markdown, HTML.
- **Token optimization** and **LLM chunking** with token estimates.
- **Batch uploads** via the web UI (50MB per file).

