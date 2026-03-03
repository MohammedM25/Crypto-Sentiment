# 🚀 Quick Deploy to Public URL

## Fastest Way: Render.com (Free, 5 minutes)

### Step 1: Deploy Backend

1. Go to: https://render.com
2. Sign up (free)
3. Click **"New +"** → **"Web Service"**
4. Connect your GitHub repo
5. Fill in:
   - **Name**: `crypto-backend`
   - **Root Directory**: `backend/crypto-backend`
   - **Runtime**: `Java`
   - **Build Command**: `./gradlew bootJar`
   - **Start Command**: `java -jar build/libs/crypto-backend-0.0.1-SNAPSHOT.jar --server.port=$PORT`

6. **Add Environment Variables** (click "Environment"):
   ```
   SPRING_PROFILES_ACTIVE=prod
   PORT=10000
   
   # Use Render's PostgreSQL (create it first)
   RDS_HOSTNAME=dpg-xxxxx-a.oregon-postgres.render.com
   RDS_PORT=5432
   RDS_DB_NAME=crypto_sentiment
   RDS_USERNAME=postgres
   RDS_PASSWORD=your-password-from-render
   
   # AWS (for SQS/S3)
   AWS_ACCESS_KEY_ID=your-key
   AWS_SECRET_ACCESS_KEY=your-secret
   AWS_REGION=us-east-2
   AWS_SQS_PROCESSING_QUEUE_URL=https://sqs.us-east-2.amazonaws.com/203129030113/crypto-sentiment-processing-queue
   AWS_S3_BUCKET_NAME=crypto-sentiment-data-mohammed-2025
   ```

7. **Create PostgreSQL Database**:
   - Click **"New +"** → **"PostgreSQL"**
   - Copy the connection details
   - Use them in backend environment variables

8. Click **"Create Web Service"**
9. Wait for deployment (~5 minutes)
10. **Copy your backend URL**: `https://crypto-backend.onrender.com`

---

### Step 2: Deploy Frontend

1. On Render, click **"New +"** → **"Static Site"**
2. Connect GitHub repo
3. Fill in:
   - **Name**: `crypto-frontend`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `build`

4. **Add Environment Variable**:
   ```
   REACT_APP_API_URL=https://crypto-backend.onrender.com/api
   ```
   (Use your actual backend URL from Step 1)

5. Click **"Create Static Site"**
6. Wait for deployment (~3 minutes)
7. **Copy your frontend URL**: `https://crypto-frontend.onrender.com`

---

## ✅ Done!

Your app is now live at:
- **Frontend**: `https://crypto-frontend.onrender.com`
- **Backend**: `https://crypto-backend.onrender.com`

Share the frontend URL with anyone!

---

## 🎯 Even Faster: Railway.app

1. Go to: https://railway.app
2. Click **"New Project"** → **"Deploy from GitHub"**
3. Select your repo
4. Railway auto-detects everything!
5. Add environment variables
6. Deploy!

Railway is even simpler but Render gives you more control.

---

## 💡 Tips

- **Free tier** works great for demos
- **Custom domain** can be added later
- **Auto-deploys** on git push
- **HTTPS** included automatically

---

## 🆘 Need Help?

If you want me to walk you through it step-by-step, just say:
- "Help me deploy to Render"
- "Help me deploy to Railway"
- "Show me the exact steps"

