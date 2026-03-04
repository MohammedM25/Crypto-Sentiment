# Render Deployment Guide (No AWS Required)

This project runs entirely on Render—no AWS account needed. Render provides a free PostgreSQL database and hosts both backend and frontend.

---

## Step 1: Deploy on Render

1. Go to **[render.com](https://render.com)** and sign in with GitHub.

2. Click **New +** → **Blueprint**.

3. Connect the **Crypto-Sentiment** repo (or select it if already connected).

4. Click **Apply**. Render will create:
   - **crypto-sentiment-db** – PostgreSQL database (free)
   - **crypto-backend** – Java API
   - **crypto-frontend** – React app

5. Wait for the first deploy (a few minutes for the Docker build).

---

## Step 2: Add data

**Option A – Demo data (instant)**  
Visit: `https://crypto-backend.onrender.com/api/trends/seed`

**Option B – Real news data (scraper)**  
The backend scrapes crypto news from CoinDesk, Cointelegraph, and CryptoNews every 2 hours. To run it immediately, visit: `https://crypto-backend.onrender.com/api/scrape`

---

## Step 3: Connect the frontend

1. In Render Dashboard, open **crypto-frontend**.

2. Go to **Environment** → **Add Environment Variable**.

3. Add:
   - **Key**: `REACT_APP_API_URL`
   - **Value**: `https://crypto-backend.onrender.com/api` (replace with your backend URL)

4. Click **Save Changes**. Render will redeploy the frontend.

---

## Summary

| Service | URL |
|---------|-----|
| Backend | `https://crypto-backend.onrender.com` |
| Frontend | `https://crypto-frontend.onrender.com` |
| Health check | `https://crypto-backend.onrender.com/health` |

- **Database**: Managed by Render, no setup required.
- **No AWS**: No RDS, SQS, S3, or credentials needed.
- **Auto-deploy**: Pushes to `main` trigger new deploys.
