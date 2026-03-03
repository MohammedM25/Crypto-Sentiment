# Local Deployment Readiness Checklist

## ✅ What's Ready

### Backend Configuration
- ✅ Spring Boot profile configured: `local-full` (default in `application.properties`)
- ✅ Database configuration: PostgreSQL on `localhost:5433`
- ✅ Server port: `8080`
- ✅ CORS configured for `localhost:3000` and `localhost:3001`
- ✅ All services enabled: `trend.controller.enabled=true`, `trend.service.enabled=true`, etc.
- ✅ JPA auto-update enabled: `spring.jpa.hibernate.ddl-auto=update`

### Frontend Configuration
- ✅ React app configured with API fallback to `/api`
- ✅ Dependencies listed in `package.json`
- ✅ TypeScript configuration present

### Database Setup
- ✅ Docker Compose file exists: `infra/docker-compose.yml`
- ✅ PostgreSQL 16 configured with correct credentials
- ✅ Port mapping: `5433:5432` (matches backend config)

## ⚠️ What Needs Attention

### 1. Frontend Environment Variable
**Issue**: Frontend defaults to `/api` which won't work for local development.

**Fix**: Create a `.env` file in the `frontend/` directory:
```bash
REACT_APP_API_URL=http://localhost:8080/api
```

Or set it when starting:
```bash
REACT_APP_API_URL=http://localhost:8080/api npm start
```

### 2. AWS Credentials (Optional but Recommended)
**Issue**: Backend tries to connect to AWS SQS/S3. If you want to test the full flow locally, you need AWS credentials.

**Options**:
- **Option A**: Configure AWS CLI (recommended)
  ```bash
  aws configure
  # Enter your AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
  ```

- **Option B**: Set environment variables
  ```bash
  export AWS_ACCESS_KEY_ID=your_key
  export AWS_SECRET_ACCESS_KEY=your_secret
  export AWS_REGION=us-east-2
  ```

- **Option C**: Run without AWS (API endpoints will work, but SQS listener won't)
  - The app will start, but SQS message processing will fail
  - You can still test API endpoints manually

### 3. Database Initialization
**Issue**: Database needs to be running before backend starts.

**Solution**: Start database first using Docker Compose.

## 📋 Step-by-Step Local Deployment

### Step 1: Start Database
```bash
cd /Users/mohammedmohammed/Crypto-Project/crypto-sentiment/infra
docker-compose up -d
```

Verify it's running:
```bash
docker ps | grep crypto-postgres
```

### Step 2: Configure Frontend (if not done)
```bash
cd /Users/mohammedmohammed/Crypto-Project/crypto-sentiment/frontend
echo "REACT_APP_API_URL=http://localhost:8080/api" > .env
```

### Step 3: Install Frontend Dependencies
```bash
cd /Users/mohammedmohammed/Crypto-Project/crypto-sentiment/frontend
npm install
```

### Step 4: Start Backend
```bash
cd /Users/mohammedmohammed/Crypto-Project/crypto-sentiment/backend/crypto-backend
./gradlew bootRun
```

Wait for: `Started CryptoBackendApplication in X.XXX seconds`

### Step 5: Start Frontend (in a new terminal)
```bash
cd /Users/mohammedmohammed/Crypto-Project/crypto-sentiment/frontend
npm start
```

### Step 6: Verify
- Backend: http://localhost:8080/health
- Frontend: http://localhost:3000
- API: http://localhost:8080/api/trends/summary

## 🔍 Verification Checklist

- [ ] Database is running (`docker ps`)
- [ ] Backend starts without errors
- [ ] Backend health check works: `curl http://localhost:8080/health`
- [ ] Frontend starts without errors
- [ ] Frontend can connect to backend (check browser console)
- [ ] API endpoints return data (may be empty initially)

## 🐛 Common Issues

### Backend won't start
- **Check**: Is PostgreSQL running? `docker ps`
- **Check**: Is port 8080 available? `lsof -i :8080`
- **Check**: Java 21 installed? `java -version`

### Frontend can't connect to backend
- **Check**: Backend is running on port 8080
- **Check**: `.env` file has correct `REACT_APP_API_URL`
- **Check**: CORS is configured (already done in `application-local-full.properties`)

### Database connection errors
- **Check**: Docker container is running: `docker ps`
- **Check**: Port 5433 is not in use: `lsof -i :5433`
- **Check**: Database credentials match: `postgres/password`

### AWS connection errors (if SQS/S3 needed)
- **Check**: AWS credentials configured: `aws configure list`
- **Check**: Region matches: `us-east-2`
- **Note**: These errors won't prevent the API from working, only SQS processing

## 📝 Summary

**Status**: ✅ **Almost Ready** - Just needs:
1. Frontend `.env` file with `REACT_APP_API_URL`
2. Database started via Docker Compose
3. Optional: AWS credentials if you want full SQS/S3 functionality

The project is well-configured for local development. The main thing missing is the frontend environment variable configuration.


