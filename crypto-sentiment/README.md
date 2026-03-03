# Crypto Sentiment Analysis Platform

A full-stack cryptocurrency sentiment analysis platform that scrapes real-time data from news sources, analyzes sentiment, and displays market trends through an interactive dashboard.

## Architecture

- **Frontend**: React + TypeScript + Tailwind CSS
- **Backend**: Spring Boot + Java 21
- **Database**: PostgreSQL (RDS for production)
- **Scraping**: Python + BeautifulSoup + VADER Sentiment
- **Cloud**: AWS Lambda + SQS + S3 + EventBridge
- **Deployment**: Elastic Beanstalk (Backend) + Netlify/Vercel (Frontend)

## Features

- Real-time cryptocurrency sentiment analysis
- Market price tracking with CoinGecko API
- Trending coins detection
- Interactive data visualization
- Multi-platform data scraping (news, forums)
- AWS cloud integration

## Quick Start

### Prerequisites
- Java 21
- Node.js 18+
- Python 3.8+
- Docker
- AWS CLI configured

### Backend Setup
```bash
cd backend/crypto-backend
./gradlew bootRun
```

### Frontend Setup
```bash
cd frontend
npm install
npm start
```

### Database Setup
```bash
docker run --name crypto-postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=crypto_sentiment -p 5433:5432 -d postgres:15
```

### Scraper Setup
```bash
cd python-service
pip install -r requirements.txt
python lambda_function.py
```

## Configuration

### Environment Variables
- `REACT_APP_API_URL`: Backend API URL (default: http://localhost:8080/api)
- `RDS_HOSTNAME`, `RDS_PORT`, `RDS_DB_NAME`, `RDS_USERNAME`, `RDS_PASSWORD`: Database config (for production)

### AWS Configuration
- **Lambda Function**: `crypto-sentiment-scraper` (runs every 15 minutes via EventBridge)
- **SQS Queue**: `crypto-sentiment-processing-queue`
- **S3 Bucket**: `crypto-sentiment-data-mohammed-2025`
- **Region**: `us-east-2`

## API Endpoints

- `GET /api/trends` - Get all sentiment trends
- `GET /api/trends/summary` - Get sentiment summary
- `GET /api/trends/trending` - Get trending coins
- `GET /api/market/top-coins` - Get top cryptocurrencies
- `GET /api/market/price?symbol=BTC` - Get price for symbol
- `GET /health` - Health check

## Data Flow

1. **Lambda Function** (triggered every 15 minutes by EventBridge)
   - Scrapes crypto news and forums
   - Analyzes sentiment using VADER
   - Sends individual messages to SQS

2. **SQS Queue** (`crypto-sentiment-processing-queue`)
   - Receives and stores messages from Lambda
   - Decouples scraping from processing

3. **Spring Boot Backend**
   - Listens to SQS queue
   - Processes sentiment data
   - Stores in PostgreSQL database
   - Backs up raw data to S3

4. **Frontend**
   - Fetches data from backend API
   - Displays sentiment trends and market data
   - Real-time visualization with Chart.js

## Development

### Backend Profiles
- `local-full`: Full features with local PostgreSQL
- `prod`: Production with AWS RDS

### Testing
```bash
# Backend tests
./gradlew test

# Frontend tests
npm test
```

## Deployment

### Local Development
```bash
# Start database
cd infra && docker-compose up -d

# Start backend
cd backend/crypto-backend && ./gradlew bootRun

# Start frontend
cd frontend && npm start
```

### Production
- **Frontend**: Deployed to Netlify/Vercel with custom domain
- **Backend**: AWS Elastic Beanstalk with custom domain
- **Database**: AWS RDS PostgreSQL
- **Scraper**: AWS Lambda (automated via EventBridge every 15 minutes)

### Domain Configuration
- **Frontend**: Set `REACT_APP_API_URL` environment variable to your backend API URL
- **Backend**: Update `cors.allowed-origins` in `application-prod.properties` with your frontend domain

## License

MIT License


