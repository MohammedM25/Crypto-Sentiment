# Deploy to Public URL - Quick Guide

## 🚀 Easiest Options (Free Tier Available)

### Option 1: Render (Recommended - Easiest)
**Best for**: Backend + Database + Frontend all in one place

#### Backend Deployment:
1. **Sign up**: https://render.com (free tier available)
2. **Create Web Service**:
   - Click "New +" → "Web Service"
   - Connect GitHub (or upload code)
   - **Root Directory**: `backend/crypto-backend`
   - **Environment**: `Java`
   - **Build Command**: `./gradlew bootJar`
   - **Start Command**: `java -jar build/libs/crypto-backend-0.0.1-SNAPSHOT.jar --server.port=$PORT`
3. **Add Environment Variables**:
   ```
   SPRING_PROFILES_ACTIVE=prod
   PORT=10000
   RDS_HOSTNAME=your-db-host
   RDS_PORT=5432
   RDS_DB_NAME=crypto_sentiment
   RDS_USERNAME=postgres
   RDS_PASSWORD=your-password
   AWS_ACCESS_KEY_ID=your-key
   AWS_SECRET_ACCESS_KEY=your-secret
   AWS_REGION=us-east-2
   AWS_SQS_PROCESSING_QUEUE_URL=https://sqs.us-east-2.amazonaws.com/203129030113/crypto-sentiment-processing-queue
   AWS_S3_BUCKET_NAME=crypto-sentiment-data-mohammed-2025
   ```
4. **Create PostgreSQL Database**:
   - Click "New +" → "PostgreSQL"
   - Copy connection string
   - Use in environment variables above

#### Frontend Deployment:
1. **Create Static Site**:
   - Click "New +" → "Static Site"
   - Connect GitHub
   - **Root Directory**: `frontend`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `build`
2. **Add Environment Variable**:
   ```
   REACT_APP_API_URL=https://your-backend.onrender.com/api
   ```
3. **Deploy** - You'll get a URL like: `https://crypto-sentiment-frontend.onrender.com`

---

### Option 2: Railway (All-in-One)
**Best for**: Simple deployment, everything together

1. **Sign up**: https://railway.app (free tier available)
2. **Create Project** → "Deploy from GitHub"
3. **Add Services**:
   - **PostgreSQL** (one-click)
   - **Backend** (Java service)
   - **Frontend** (Static site)

Railway auto-detects and configures everything!

---

### Option 3: Vercel (Frontend) + Render (Backend)
**Best for**: Best performance, separate services

#### Frontend on Vercel:
1. **Sign up**: https://vercel.com
2. **Import Project** from GitHub
3. **Root Directory**: `frontend`
4. **Environment Variable**:
   ```
   REACT_APP_API_URL=https://your-backend.onrender.com/api
   ```
5. **Deploy** - Get URL: `https://crypto-sentiment.vercel.app`

#### Backend on Render:
(Follow Option 1 backend steps above)

---

## 📋 Quick Deploy Checklist

### Backend:
- [ ] Choose platform (Render/Railway)
- [ ] Connect GitHub repository
- [ ] Set root directory: `backend/crypto-backend`
- [ ] Add environment variables
- [ ] Deploy
- [ ] Copy backend URL

### Frontend:
- [ ] Choose platform (Vercel/Render)
- [ ] Connect GitHub repository  
- [ ] Set root directory: `frontend`
- [ ] Add `REACT_APP_API_URL` = your backend URL
- [ ] Deploy
- [ ] Copy frontend URL

### Database:
- [ ] Create PostgreSQL database
- [ ] Copy connection string
- [ ] Add to backend environment variables

---

## 🔗 Getting Your Public URLs

After deployment, you'll get:
- **Frontend URL**: `https://your-app.vercel.app` or `https://your-app.onrender.com`
- **Backend URL**: `https://your-backend.onrender.com` or `https://your-backend.railway.app`

Share these URLs with anyone!

---

## 🎯 Simplest Path (5 minutes)

1. **Render.com** (easiest):
   ```bash
   # 1. Sign up at render.com
   # 2. Click "New Web Service"
   # 3. Connect GitHub
   # 4. Select backend/crypto-backend
   # 5. Add environment variables
   # 6. Deploy!
   ```

2. **Frontend**:
   ```bash
   # 1. Click "New Static Site" on Render
   # 2. Select frontend directory
   # 3. Add REACT_APP_API_URL = your backend URL
   # 4. Deploy!
   ```

---

## 💡 Pro Tips

- **Free tiers** available on all platforms
- **Custom domains** can be added later
- **Auto-deploy** on git push (if connected to GitHub)
- **SSL/HTTPS** included automatically

---

## 🆘 Need Help?

If you want me to help you deploy right now, tell me which platform you prefer:
- Render (easiest)
- Railway (simplest)
- Vercel + Render (best performance)

