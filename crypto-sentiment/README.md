# Crypto Sentiment Analysis Platform

A full-stack cryptocurrency sentiment analysis platform with market trends and an interactive dashboard.

## Architecture

- **Frontend**: React + TypeScript + Tailwind CSS
- **Backend**: Spring Boot + Java 21
- **Database**: PostgreSQL (Render Postgres in production)
- **Deployment**: Render (backend + frontend + database)

## Quick Start

### Prerequisites
- Java 21
- Node.js 18+
- Docker (for local DB)

### Local Development
```bash
# Start database
cd infra && docker-compose up -d

# Start backend
cd backend/crypto-backend && ./gradlew bootRun

# Start frontend
cd frontend && npm install && npm start
```

### Add Demo Data
Visit `http://localhost:8080/api/trends/seed` or use:
```bash
curl http://localhost:8080/api/trends/seed
```

## Deployment (Render)

See [RENDER_DEPLOYMENT.md](RENDER_DEPLOYMENT.md) for full instructions.

1. Push to GitHub
2. Connect repo to Render → New Blueprint → Apply
3. Visit `https://crypto-backend.onrender.com/api/trends/seed` to add demo data
4. Set `REACT_APP_API_URL` in the frontend service

## API Endpoints

- `GET /api/trends` - Get all sentiment trends
- `GET /api/trends/summary` - Get sentiment summary
- `GET /api/trends/trending` - Get trending coins
- `GET /api/trends/seed` - Add demo data
- `GET /api/market/top-coins` - Get top cryptocurrencies
- `GET /api/market/price?symbol=BTC` - Get price for symbol
- `GET /health` - Health check

## License

MIT License
