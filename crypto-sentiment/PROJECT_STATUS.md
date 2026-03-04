# Crypto Sentiment - Project Status

## Components

| Component | Status | Location |
|-----------|--------|----------|
| Backend (Spring Boot) | ✅ Complete | `backend/crypto-backend/` |
| Frontend (React) | ✅ Complete | `frontend/` |
| Database (PostgreSQL) | ✅ Configured | `infra/docker-compose.yml` |
| Deployment | ✅ Render | See RENDER_DEPLOYMENT.md |

## Quick Start (Local)

```bash
# 1. Start database
cd infra && docker-compose up -d

# 2. Start backend
cd backend/crypto-backend && ./gradlew bootRun

# 3. Start frontend
cd frontend && npm start

# 4. Add demo data
curl http://localhost:8080/api/trends/seed
```

## Deployment

Deploy to Render with one click. See [RENDER_DEPLOYMENT.md](RENDER_DEPLOYMENT.md).
