# 📈 Crypto Sentiment Analysis

Real-time cryptocurrency sentiment analysis platform. Tracks social buzz from crypto news, analyzes market mood, and surfaces trending coins—all without AWS.

![Status](https://img.shields.io/badge/Status-Live-brightgreen)
![React](https://img.shields.io/badge/React-18.0-blue)
![TypeScript](https://img.shields.io/badge/TypeScript-4.9-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![Java](https://img.shields.io/badge/Java-21-orange)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

> **RIP AWS free tier** — Originally ran on AWS (Lambda, SQS, S3, RDS, Elastic Beanstalk). Now fully on [Render](https://render.com) — backend, frontend, and database — zero cloud bills.

## ✨ Features

- **Live Dashboard** — Sentiment trends, trending coins, market prices (CoinGecko), interactive charts
- **Automated Scraper** — Fetches headlines from CoinDesk, Cointelegraph, CryptoNews every 2 hours
- **Market Data** — Top 100 cryptos, real-time price lookup

## 🛠️ Tech Stack

React 18 · TypeScript · Tailwind CSS · Chart.js · Spring Boot 3.5 · Java 21 · PostgreSQL · Jsoup · Render

## 🚀 Live Demo

**[Dashboard](https://crypto-frontend-lpq9.onrender.com)** · **[API](https://crypto-backend-jnf0.onrender.com)**

> Free tier: backend sleeps after 15 min — first load may take ~50 seconds.

## 📁 Structure

```
├── crypto-sentiment/
│   ├── frontend/          # React + TypeScript
│   ├── backend/           # Spring Boot
│   └── infra/             # Docker Compose (local DB)
└── render.yaml            # One-click Render deploy
```

## 🏃 Quick Start

```bash
cd crypto-sentiment/infra && docker-compose up -d
cd crypto-sentiment/backend/crypto-backend && ./gradlew bootRun   # Terminal 1
cd crypto-sentiment/frontend && npm install && npm start          # Terminal 2
```

Data auto-populates from news on startup. Manual trigger: `http://localhost:8080/api/scrape`

## 🚀 Deploy (Render)

1. Push to GitHub
2. [Render](https://render.com) → **New** → **Blueprint** → Connect repo → **Apply**
3. Set `REACT_APP_API_URL` in frontend service
4. Data populates from news on startup; hit `/api/scrape` to refresh

## 👨‍💻 Author

**Mohammed Mohammed** — Computer Engineering @ UIUC · [GitHub](https://github.com/MohammedM25)

