# Fly.io Deployment Guide

## Why Fly.io?
- ✅ **Free tier**: 3 shared-cpu VMs, 3GB storage
- ✅ **Perfect for Spring Boot**: Full Java runtime
- ✅ **Custom domains**: Easy setup
- ✅ **Fast deployment**: 5-10 minutes
- ✅ **Good documentation**: Well-supported

## Quick Steps

### 1. Sign Up & Install CLI
```bash
# Sign up at fly.io (free)
# Then install flyctl
curl -L https://fly.io/install.sh | sh

# Or on Mac:
brew install flyctl

# Login
flyctl auth login
```

### 2. Initialize Fly.io App
```bash
cd backend/crypto-backend
flyctl launch
```

This will:
- Create a `fly.toml` config file
- Ask you to name your app (e.g., `crypto-backend`)
- Detect Java/Spring Boot
- Set up deployment

### 3. Configure fly.toml
The generated `fly.toml` should look like this:

```toml
app = "crypto-backend"
primary_region = "iad"  # or your preferred region

[build]
  builder = "paketobuildpacks/builder:base"

[env]
  PORT = "8080"
  SPRING_PROFILES_ACTIVE = "prod"

[[services]]
  internal_port = 8080
  protocol = "tcp"

  [[services.ports]]
    port = 80
    handlers = ["http"]
    force_https = true

  [[services.ports]]
    port = 443
    handlers = ["tls", "http"]
```

### 4. Add Environment Variables
```bash
flyctl secrets set \
  RDS_HOSTNAME=crypto-sentiment-db.crusa8weoxnc.us-east-2.rds.amazonaws.com \
  RDS_PORT=5432 \
  RDS_DB_NAME=crypto_sentiment \
  RDS_USERNAME=dbadmin \
  RDS_PASSWORD=CryptoPassword123! \
  AWS_ACCESS_KEY_ID=your_access_key \
  AWS_SECRET_ACCESS_KEY=your_secret_key \
  AWS_REGION=us-east-2 \
  AWS_SQS_PROCESSING_QUEUE_URL=https://sqs.us-east-2.amazonaws.com/203129030113/crypto-sentiment-processing-queue \
  AWS_S3_BUCKET_NAME=crypto-sentiment-data-mohammed-2025 \
  SPRING_PROFILES_ACTIVE=prod
```

### 5. Deploy
```bash
flyctl deploy
```

### 6. Add Custom Domain
```bash
# Add your domain
flyctl domains add api.cryptosentiment.it.com

# Fly.io will give you DNS records to add
# Add CNAME in your NS1 DNS settings
```

### 7. Update DNS (NS1)
In your NS1 DNS settings for `cryptosentiment.it.com`:

1. Add CNAME record:
   - **Name**: `api`
   - **Type**: `CNAME`
   - **Value**: (Fly.io will provide, e.g., `crypto-backend.fly.dev`)
   - **TTL**: 300

2. Wait 5-10 minutes for DNS propagation

3. Test: `curl https://api.cryptosentiment.it.com/health`

## Alternative: Dockerfile (if needed)

If Fly.io doesn't auto-detect Spring Boot, create `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Update Frontend

In your Vercel project, add environment variable:

```
REACT_APP_API_URL=https://api.cryptosentiment.it.com/api
```

Then redeploy your frontend.

## Troubleshooting

- **Build fails**: Check `flyctl logs`
- **Can't connect to RDS**: Verify security group allows Fly.io IPs
- **DNS not working**: Wait 10-15 min, check NS1 DNS records
- **App crashes**: Check `flyctl logs` for errors
- **Port issues**: Fly.io uses PORT env var (set to 8080)

## Fly.io Free Tier Limits

- 3 shared-cpu VMs
- 3GB storage
- 160GB outbound data transfer/month
- Free SSL certificates
- Custom domains supported

## Useful Commands

```bash
# View logs
flyctl logs

# SSH into app
flyctl ssh console

# Check status
flyctl status

# Scale (if needed)
flyctl scale count 1
```


