# Add HTTPS to Beanstalk - Step by Step

## Quick Setup (10-15 minutes)

### Step 1: Get SSL Certificate (5 minutes)

1. **Go to AWS Certificate Manager**
   - AWS Console → Certificate Manager
   - Region: **us-east-2** (Ohio)
   - Click **Request certificate**

2. **Request Public Certificate**
   - Domain name: `cryptosent.us-east-2.elasticbeanstalk.com`
   - OR use: `api.cryptosentiment.it.com` (if you want subdomain)
   - Validation: **DNS validation** (recommended)
   - Click **Request**

3. **Validate Certificate**
   - Click on the certificate
   - Click **Create record in Route 53** (if using Route 53)
   - OR manually add CNAME record to your DNS provider
   - Wait for validation (usually 5-10 minutes)

### Step 2: Add HTTPS Listener to Beanstalk (5 minutes)

1. **Go to Elastic Beanstalk**
   - AWS Console → Elastic Beanstalk
   - Select: `Crypto-Sentiment-Analysis-Projec-env`

2. **Configure Load Balancer**
   - Click **Configuration** (left sidebar)
   - Scroll to **Load balancer**
   - Click **Edit**

3. **Add HTTPS Listener**
   - Under **Listeners**, click **Add listener**
   - **Port**: `443`
   - **Protocol**: `HTTPS`
   - **SSL certificate**: Select your certificate from ACM
   - **Process**: `default` (port 80)
   - Click **Add**

4. **Save Configuration**
   - Click **Apply** at bottom
   - Wait for environment to update (~2-3 minutes)

### Step 3: Update Frontend (1 minute)

1. **Update Environment Variable in Vercel**
   - Go to: Vercel Dashboard → Your Project → Settings → Environment Variables
   - Find: `REACT_APP_API_URL`
   - Change to: `https://cryptosent.us-east-2.elasticbeanstalk.com/api`
   - Save

2. **Redeploy Frontend**
   - Go to: Deployments tab
   - Click **Redeploy** on latest deployment
   - OR: `cd frontend && npx vercel --prod`

### Step 4: Test

1. Visit your frontend
2. Open DevTools → Network tab
3. Check API calls - should be HTTPS now
4. No more mixed content errors!

## Alternative: Use Subdomain (api.cryptosentiment.it.com)

If you want a cleaner URL:

1. **Request certificate for**: `api.cryptosentiment.it.com`
2. **Add CNAME record** in your DNS:
   - Name: `api`
   - Value: `cryptosent.us-east-2.elasticbeanstalk.com`
3. **Use in frontend**: `https://api.cryptosentiment.it.com/api`

## Troubleshooting

**Certificate validation taking too long:**
- Check DNS records are correct
- Wait up to 30 minutes (usually 5-10)

**HTTPS not working after setup:**
- Check security group allows port 443
- Verify certificate is validated
- Check Beanstalk environment is healthy

**Still getting mixed content:**
- Clear browser cache
- Hard refresh (Ctrl+Shift+R or Cmd+Shift+R)
- Check environment variable is updated

## Quick Reference

**Certificate Manager**: https://console.aws.amazon.com/acm/home?region=us-east-2  
**Beanstalk Console**: https://console.aws.amazon.com/elasticbeanstalk/home?region=us-east-2  
**Backend URL (after HTTPS)**: `https://cryptosent.us-east-2.elasticbeanstalk.com`


