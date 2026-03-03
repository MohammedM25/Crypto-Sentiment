# Elastic Beanstalk Manual Setup Guide

## Prerequisites
- AWS account
- AWS CLI configured
- Your Spring Boot JAR file built
- Domain: cryptosentiment.it.com

## Step 1: Build Your JAR

```bash
cd backend/crypto-backend
./gradlew bootJar
```

This creates: `build/libs/crypto-backend-0.0.1-SNAPSHOT.jar`

## Step 2: Create Beanstalk Application

In AWS Console:
1. Go to Elastic Beanstalk
2. Click "Create Application"
3. Application name: `crypto-sentiment-app`
4. Platform: **Java** → **Corretto 21 running on 64bit Amazon Linux 2023**
5. Platform branch: `Corretto 21 running on 64bit Amazon Linux 2023/4.7.1`
6. Application code: Upload your JAR file
7. Click "Create application"

## Step 3: Configure Environment

After application is created:
1. Click "Create environment"
2. Environment tier: **Web server environment**
3. Application: `crypto-sentiment-app`
4. Environment name: `crypto-sentiment-prod`
5. Domain: Leave default (or use your custom domain later)
6. Platform: **Java** → **Corretto 21 running on 64bit Amazon Linux 2023**
7. Platform branch: `Corretto 21 running on 64bit Amazon Linux 2023/4.7.1`
8. Application code: Upload your JAR file
9. Click "Configure more options"

## Step 4: Configure More Options

### 4a. Software Configuration
- Environment properties → Add:
  ```
  SPRING_PROFILES_ACTIVE=prod
  PORT=5000
  
  RDS_HOSTNAME=crypto-sentiment-db.crusa8weoxnc.us-east-2.rds.amazonaws.com
  RDS_PORT=5432
  RDS_DB_NAME=crypto_sentiment
  RDS_USERNAME=dbadmin
  RDS_PASSWORD=CryptoPassword123!
  
  AWS_ACCESS_KEY_ID=your_access_key
  AWS_SECRET_ACCESS_KEY=your_secret_key
  AWS_REGION=us-east-2
  
  AWS_SQS_PROCESSING_QUEUE_URL=https://sqs.us-east-2.amazonaws.com/203129030113/crypto-sentiment-processing-queue
  AWS_S3_BUCKET_NAME=crypto-sentiment-data-mohammed-2025
  ```

### 4b. Instance Configuration
- Instance profile: Create new or use existing
  - If creating new, name it: `crypto-sentiment-eb-role`
  - Attach policies:
    - `AmazonSQSFullAccess`
    - `AmazonS3FullAccess`
    - `AWSElasticBeanstalkWebTier`
    - `AWSElasticBeanstalkWorkerTier`

### 4c. Capacity Configuration
- Environment type: **Single instance** (for free tier)
- Or **Load balanced** (if you want auto-scaling)

### 4d. Load Balancer (if using load balanced)
- Type: Application Load Balancer
- Listeners: HTTP (port 80) and HTTPS (port 443)

### 4e. Rolling Updates and Deployments
- Deployment policy: **All at once** (faster) or **Rolling** (safer)

Click "Create environment"

## Step 5: Create Procfile (Important!)

After environment is created:
1. Go to your local project
2. Create file: `backend/crypto-backend/Procfile`
3. Content:
   ```
   web: java -jar crypto-backend-0.0.1-SNAPSHOT.jar --server.port=$PORT
   ```
4. Add Procfile to your JAR:
   ```bash
   cd backend/crypto-backend
   jar -uf build/libs/crypto-backend-0.0.1-SNAPSHOT.jar Procfile
   ```

## Step 6: Create Deployment ZIP

```bash
cd backend/crypto-backend/build/libs
zip deploy.zip crypto-backend-0.0.1-SNAPSHOT.jar
```

## Step 7: Upload and Deploy

### Option A: Via AWS Console
1. Go to your Beanstalk environment
2. Click "Upload and deploy"
3. Upload `deploy.zip`
4. Version label: `v1` (or any name)
5. Click "Deploy"

### Option B: Via AWS CLI
```bash
# Upload to S3
aws s3 cp deploy.zip s3://elasticbeanstalk-us-east-2-YOUR-ACCOUNT-ID/crypto-backend-v1.zip --region us-east-2

# Create application version
aws elasticbeanstalk create-application-version \
  --application-name crypto-sentiment-app \
  --version-label v1 \
  --source-bundle S3Bucket=elasticbeanstalk-us-east-2-YOUR-ACCOUNT-ID,S3Key=crypto-backend-v1.zip \
  --region us-east-2

# Deploy
aws elasticbeanstalk update-environment \
  --application-name crypto-sentiment-app \
  --environment-name crypto-sentiment-prod \
  --version-label v1 \
  --region us-east-2
```

## Step 8: Verify RDS Security Group

1. Go to RDS → Your database → Connectivity & security
2. Click on Security group
3. Inbound rules → Edit
4. Add rule:
   - Type: PostgreSQL
   - Port: 5432
   - Source: Security group of your Beanstalk environment
   - Or: 0.0.0.0/0 (less secure, but works)

## Step 9: Check Application Logs

1. Go to Beanstalk environment
2. Click "Logs" → "Request logs" → "Last 100 lines"
3. Check for errors

Common issues:
- Java version mismatch → Use Java 21 platform
- IAM permissions → Add SQS/S3 policies
- Database connection → Check security group
- Port binding → Procfile should use $PORT

## Step 10: Add Custom Domain

1. Go to Beanstalk environment → Configuration
2. Load balancer → Edit
3. Add listener rule for your domain
4. Or use Route 53/CloudFront for domain management

For custom domain setup:
1. Get your Beanstalk URL: `crypto-sentiment-prod.eba-xxx.us-east-2.elasticbeanstalk.com`
2. In your DNS (NS1):
   - Add CNAME: `api` → `crypto-sentiment-prod.eba-xxx.us-east-2.elasticbeanstalk.com`
3. Wait 5-10 minutes for DNS propagation

## Step 11: Update Frontend

In Vercel, add environment variable:
```
REACT_APP_API_URL=https://api.cryptosentiment.it.com/api
```

Or use the Beanstalk URL directly:
```
REACT_APP_API_URL=https://crypto-sentiment-prod.eba-xxx.us-east-2.elasticbeanstalk.com/api
```

## Troubleshooting

### App not starting
- Check logs in Beanstalk console
- Verify Java 21 platform
- Check Procfile is in JAR
- Verify environment variables

### 502 Bad Gateway
- App not listening on correct port
- Check Procfile uses $PORT
- Verify server.address=0.0.0.0 in application-prod.properties

### Database connection failed
- Check RDS security group allows Beanstalk security group
- Verify RDS credentials in environment variables

### IAM permissions error
- Add SQS and S3 policies to instance profile
- Verify instance profile is attached to environment

## Important Configuration Files

### application-prod.properties
```
server.port=${PORT:5000}
server.address=0.0.0.0

spring.datasource.url=jdbc:postgresql://${RDS_HOSTNAME}:${RDS_PORT}/${RDS_DB_NAME}?connectTimeout=10&socketTimeout=30
spring.datasource.username=${RDS_USERNAME}
spring.datasource.password=${RDS_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

cors.allowed-origins=https://cryptosentiment.it.com,https://www.cryptosentiment.it.com

aws.region=us-east-2
aws.sqs.processing-queue-url=https://sqs.us-east-2.amazonaws.com/203129030113/crypto-sentiment-processing-queue
aws.s3.bucket-name=crypto-sentiment-data-mohammed-2025
```

### Procfile
```
web: java -jar crypto-backend-0.0.1-SNAPSHOT.jar --server.port=$PORT
```

## Quick Checklist

- [ ] JAR file built
- [ ] Procfile created and added to JAR
- [ ] Beanstalk application created
- [ ] Environment created with Java 21 platform
- [ ] Environment variables added
- [ ] IAM instance profile with SQS/S3 permissions
- [ ] RDS security group updated
- [ ] Deployment ZIP created
- [ ] Deployed to Beanstalk
- [ ] Logs checked (no errors)
- [ ] Custom domain configured (optional)
- [ ] Frontend updated with new API URL


