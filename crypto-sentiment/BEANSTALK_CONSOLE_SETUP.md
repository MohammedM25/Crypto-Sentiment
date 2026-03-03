# Beanstalk Console Configuration Checklist

Your environment: **Crypto-Sentiment-Analysis-Projec-env**
URL: **http://cryptosent.us-east-2.elasticbeanstalk.com**

## ✅ Already Done (via CLI)
- IAM policies attached (SQS, S3)
- IAM role exists: `crypto-sentiment-eb-role`

## ⚠️ Do in AWS Console (when environment is Ready)

### 1. Add Environment Variables
Go to: **Elastic Beanstalk → Your Environment → Configuration → Software → Edit**

Add these environment properties:

```
SPRING_PROFILES_ACTIVE=prod
PORT=5000

RDS_HOSTNAME=crypto-sentiment-db.crusa8weoxnc.us-east-2.rds.amazonaws.com
RDS_PORT=5432
RDS_DB_NAME=crypto_sentiment
RDS_USERNAME=dbadmin
RDS_PASSWORD=CryptoPassword123!

AWS_REGION=us-east-2
AWS_SQS_PROCESSING_QUEUE_URL=https://sqs.us-east-2.amazonaws.com/203129030113/crypto-sentiment-processing-queue
AWS_S3_BUCKET_NAME=crypto-sentiment-data-mohammed-2025

AWS_ACCESS_KEY_ID=your_access_key_here
AWS_SECRET_ACCESS_KEY=your_secret_key_here
```

Click **Apply**

### 2. Attach Instance Profile
Go to: **Configuration → Security → Edit**

- **IAM instance profile**: Select `crypto-sentiment-eb-profile`
- If it doesn't exist, create it:
  - IAM → Roles → `crypto-sentiment-eb-role` → Attach policies:
    - AmazonSQSFullAccess
    - AmazonS3FullAccess
    - AWSElasticBeanstalkWebTier
    - AWSElasticBeanstalkWorkerTier
  - IAM → Instance profiles → Create → Name: `crypto-sentiment-eb-profile`
  - Add role: `crypto-sentiment-eb-role`

Click **Apply**

### 3. Configure Load Balancer (Optional - for HTTPS)
Go to: **Configuration → Load balancer → Edit**

**Listeners:**
- Port 80: HTTP → HTTP (port 80) ✅ (should already exist)
- Port 443: HTTPS → HTTPS (port 443) - Add this for SSL

**For HTTPS:**
1. First, create SSL certificate in **AWS Certificate Manager (ACM)**
   - Request certificate for: `api.cryptosentiment.it.com`
   - Validate domain (add DNS records in NS1)
2. Then add HTTPS listener with certificate ARN

### 4. Deploy Your Application
1. Build JAR:
   ```bash
   cd backend/crypto-backend
   ./gradlew bootJar
   jar -uf build/libs/crypto-backend-0.0.1-SNAPSHOT.jar Procfile
   ```

2. Create ZIP:
   ```bash
   cd build/libs
   zip deploy.zip crypto-backend-0.0.1-SNAPSHOT.jar
   ```

3. Upload to Beanstalk:
   - Go to your environment
   - Click **Upload and deploy**
   - Upload `deploy.zip`
   - Version label: `v1`
   - Click **Deploy**

### 5. Test
```bash
curl http://cryptosent.us-east-2.elasticbeanstalk.com/health
```

## Quick Reference

**Environment Name:** Crypto-Sentiment-Analysis-Projec-env  
**Application:** Crypto-Sentiment-Analysis-Project  
**URL:** http://cryptosent.us-east-2.elasticbeanstalk.com  
**IAM Role:** crypto-sentiment-eb-role  
**IAM Profile:** crypto-sentiment-eb-profile  

## Troubleshooting

- **502 Bad Gateway**: Check logs, verify Procfile is in JAR
- **Can't connect to RDS**: Check security group allows Beanstalk
- **IAM errors**: Verify instance profile is attached
- **App not starting**: Check environment variables are set


