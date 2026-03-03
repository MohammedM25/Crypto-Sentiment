# Connect Everything - Complete Guide

## ✅ Current Status

### Backend (Beanstalk)
- **URL**: `http://cryptosent.us-east-2.elasticbeanstalk.com`
- **Status**: ✅ Working
- **Health**: OK
- **Database**: Connected (10,687 posts)
- **SQS**: Connected
- **S3**: Connected

### Lambda Scraper
- **Status**: ✅ Active
- **EventBridge**: Running every 15 minutes
- **SQS**: Sending to `crypto-sentiment-processing-queue`

### Frontend
- **Domain**: `cryptosentiment.it.com` (Vercel)
- **Status**: ⚠️ Needs to connect to Beanstalk backend

## 🔧 Steps to Connect Everything

### 1. Rebuild Backend with Updated CORS
```bash
cd backend/crypto-backend
./gradlew bootJar
jar -uf build/libs/crypto-backend-0.0.1-SNAPSHOT.jar Procfile
cd build/libs
zip deploy.zip crypto-backend-0.0.1-SNAPSHOT.jar
```

Then upload `deploy.zip` to Beanstalk:
- Go to AWS Console → Elastic Beanstalk
- Select your environment
- Click "Upload and deploy"
- Upload `deploy.zip`

### 2. Deploy Frontend to Vercel
```bash
cd frontend
npm run build
```

In Vercel:
- Go to your project settings
- Add environment variable:
  - **Name**: `REACT_APP_API_URL`
  - **Value**: `http://cryptosent.us-east-2.elasticbeanstalk.com/api`
- Redeploy

Or if using CLI:
```bash
vercel env add REACT_APP_API_URL production
# Enter: http://cryptosent.us-east-2.elasticbeanstalk.com/api
vercel --prod
```

### 3. Verify Connections

**Test Backend:**
```bash
curl http://cryptosent.us-east-2.elasticbeanstalk.com/health
```

**Test Frontend → Backend:**
- Open `https://cryptosentiment.it.com`
- Check browser console for API calls
- Should see data loading

**Test Lambda → SQS → Backend:**
- Check Beanstalk logs for SQS messages
- Check database for new trends
- Lambda runs every 15 minutes automatically

## 🔗 Connection Flow

```
Frontend (Vercel)
    ↓
    API Calls
    ↓
Backend (Beanstalk)
    ↓
    Reads from
    ↓
PostgreSQL (RDS)
    ↑
    Writes to
    ↑
Backend (SQS Listener)
    ↑
    Receives from
    ↑
SQS Queue
    ↑
    Sends to
    ↑
Lambda Scraper (EventBridge every 15 min)
```

## 📋 Quick Checklist

- [x] Backend deployed to Beanstalk
- [x] Lambda scraper active
- [x] EventBridge trigger set up
- [x] Database connected
- [x] SQS queue working
- [x] S3 bucket connected
- [x] CORS updated
- [ ] Backend JAR rebuilt with CORS
- [ ] Backend redeployed to Beanstalk
- [ ] Frontend environment variable set
- [ ] Frontend redeployed to Vercel
- [ ] Test end-to-end connection

## 🐛 Troubleshooting

**Frontend can't connect:**
- Check CORS in backend logs
- Verify `REACT_APP_API_URL` is set in Vercel
- Check browser console for CORS errors

**No data in frontend:**
- Check backend health endpoint
- Verify database has data
- Check backend logs for errors

**Lambda not running:**
- Check EventBridge rule is enabled
- Check Lambda function logs
- Verify SQS queue exists

**Backend 502 error:**
- Check Beanstalk logs
- Verify Procfile is in JAR
- Check environment variables are set


