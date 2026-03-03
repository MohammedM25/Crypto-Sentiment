# Render Deployment Guide

## Option A: One-Click Blueprint (recommended)

1. **Push the repo to GitHub** (this project must be in a Git repo connected to Render).

2. **Sign up / log in at [render.com](https://render.com)** (GitHub login).

3. **Create from Blueprint**
   - Dashboard → **New +** → **Blueprint**
   - Connect the repository that contains this project (e.g. `Crypto-Project` or `crypto-sentiment`)
   - Render will detect `render.yaml` at the repo root and show the **crypto-backend** and **crypto-frontend** services
   - Click **Apply**

4. **Set secret environment variables** (Dashboard → **crypto-backend** → **Environment**)
   - `RDS_HOSTNAME` – your Postgres host (e.g. AWS RDS)
   - `RDS_DB_NAME` – database name
   - `RDS_USERNAME` – database user
   - `RDS_PASSWORD` – database password  
   - Optionally: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` if the app uses AWS (SQS/S3)

5. **Optional – Frontend**
   - If you use the **crypto-frontend** service on Render, set **REACT_APP_API_URL** to your backend URL (e.g. `https://crypto-backend.onrender.com/api`) in the frontend service’s Environment tab.

6. **Deploy**
   - First deploy may take a few minutes (Docker build). After that, pushes to the connected branch auto-deploy.

---

## Option B: Manual Web Service (no Blueprint)

1. **Sign up at render.com** (use GitHub login)

2. **Create New Web Service**
   - Click "New +" → "Web Service"
   - Connect your GitHub account (if not already)
   - Select the repository that contains this project
   - Set **Root Directory**: `crypto-sentiment/backend/crypto-backend`
   - **Name**: `crypto-backend` (or any name)
   - **Region**: Choose closest (e.g., Ohio)
   - **Branch**: `main` (or your default branch)
   - **Runtime**: `Docker` (uses the repo’s Dockerfile)
   - Leave build/start command empty when using Docker

3. **Add Environment Variables**
   Go to Environment tab and add:

   ```
   SPRING_PROFILES_ACTIVE=prod
   PORT=10000
   
   # RDS Database
   RDS_HOSTNAME=crypto-sentiment-db.crusa8weoxnc.us-east-2.rds.amazonaws.com
   RDS_PORT=5432
   RDS_DB_NAME=crypto_sentiment
   RDS_USERNAME=dbadmin
   RDS_PASSWORD=CryptoPassword123!
   
   # AWS Credentials (for SQS/S3)
   AWS_ACCESS_KEY_ID=your_access_key
   AWS_SECRET_ACCESS_KEY=your_secret_key
   AWS_REGION=us-east-2
   
   # AWS Services
   AWS_SQS_PROCESSING_QUEUE_URL=https://sqs.us-east-2.amazonaws.com/203129030113/crypto-sentiment-processing-queue
   AWS_S3_BUCKET_NAME=crypto-sentiment-data-mohammed-2025
   ```

4. **Add Custom Domain**
   - Go to Settings → Custom Domains
   - Click "Add Custom Domain"
   - Enter: `api.cryptosentiment.it.com`
   - Render will give you DNS records
   - Add CNAME in your NS1 DNS settings

5. **Deploy!**
   - Click "Manual Deploy" → "Deploy latest commit"
   - Or push to GitHub (auto-deploys)
   - Check logs to see if it starts successfully

## DNS Setup (NS1)

In your NS1 DNS settings for `cryptosentiment.it.com`:

1. Add CNAME record:
   - **Name**: `api`
   - **Type**: `CNAME`
   - **Value**: (Render will provide this, e.g., `xxx.onrender.com`)
   - **TTL**: 300

2. Wait 5-10 minutes for DNS propagation

3. Test: `curl https://api.cryptosentiment.it.com/health`

## Update Frontend

In your Vercel project, add environment variable:

```
REACT_APP_API_URL=https://api.cryptosentiment.it.com/api
```

Then redeploy your frontend.

## RDS Security Group

Render's IPs are dynamic. You have two options:

**Option 1: Allow all IPs (easier, less secure)**
- RDS Security Group → Inbound Rules
- Add: PostgreSQL (5432) from 0.0.0.0/0

**Option 2: Use AWS VPC Peering (more secure)**
- More complex setup
- Not needed for a resume project

For now, Option 1 is fine.

## Troubleshooting

- **Build fails**: Check Render logs, ensure Java 21 is available
- **Can't connect to RDS**: Verify security group allows 0.0.0.0/0 on port 5432
- **DNS not working**: Wait 10-15 min, check NS1 DNS records
- **App crashes**: Check Render logs for errors
- **Port issues**: Render uses $PORT environment variable (set to 10000)

## Render Free Tier Limits

- 750 hours/month (enough for 24/7)
- Sleeps after 15 min of inactivity (wakes on request)
- Free SSL certificates
- Custom domains supported


