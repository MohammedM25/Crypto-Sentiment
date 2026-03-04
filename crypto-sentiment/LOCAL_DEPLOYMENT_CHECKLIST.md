# Local Deployment Checklist

## Prerequisites
- Docker (for PostgreSQL)
- Java 21
- Node.js 18+

## Steps

1. **Start database**
   ```bash
   cd infra && docker-compose up -d
   ```

2. **Start backend**
   ```bash
   cd backend/crypto-backend && ./gradlew bootRun
   ```
   Uses `local-full` profile with PostgreSQL on `localhost:5433`.

3. **Start frontend**
   ```bash
   cd frontend
   echo "REACT_APP_API_URL=http://localhost:8080/api" > .env
   npm install && npm start
   ```

4. **Add demo data**
   ```bash
   curl http://localhost:8080/api/trends/seed
   ```

## Verify
- Backend: http://localhost:8080/health
- Frontend: http://localhost:3000
