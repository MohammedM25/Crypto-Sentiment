# Fix Certificate Request - Use Custom Domain

## Problem
You cannot request a certificate for `cryptosent.us-east-2.elasticbeanstalk.com` because AWS owns that domain.

## Solution: Use Your Custom Domain

### Step 1: Delete Failed Certificate
1. In Certificate Manager, click **Delete** on the failed certificate
2. Confirm deletion

### Step 2: Request New Certificate for Custom Domain

1. **Click "Request certificate"** again
2. **Domain name**: Enter `api.cryptosentiment.it.com`
   - This creates a subdomain for your API
   - You can use this subdomain for the backend
3. **Validation**: DNS validation (recommended)
4. **Key algorithm**: RSA 2048
5. Click **Request**

### Step 3: Add DNS Records for Validation

After requesting, AWS will show you CNAME records like:
```
Name: _abc123.api.cryptosentiment.it.com
Value: _xyz789.acm-validations.aws.
```

**Add these to your DNS provider** (wherever you manage `cryptosentiment.it.com`):
- Go to your DNS provider (NS1, Route 53, etc.)
- Add the CNAME record AWS provides
- Wait 5-10 minutes for validation

### Step 4: Point Subdomain to Beanstalk

Once certificate is validated, add another DNS record:

**CNAME Record:**
- **Name**: `api` (or `api.cryptosentiment.it.com`)
- **Value**: `cryptosent.us-east-2.elasticbeanstalk.com`
- **TTL**: 3600

This makes `api.cryptosentiment.it.com` point to your Beanstalk backend.

### Step 5: Add HTTPS Listener to Beanstalk

1. Go to: Beanstalk → Your Environment → Configuration → Load Balancer
2. Click **Edit**
3. Add listener:
   - **Port**: 443
   - **Protocol**: HTTPS
   - **SSL certificate**: Select `api.cryptosentiment.it.com` certificate
   - **Process**: default (port 80)
4. Click **Apply**

### Step 6: Update Frontend

1. In Vercel → Settings → Environment Variables
2. Update `REACT_APP_API_URL` to: `https://api.cryptosentiment.it.com/api`
3. Redeploy frontend

## Alternative: Use Main Domain

If you prefer to use `cryptosentiment.it.com` for the backend:
- Request certificate for: `cryptosentiment.it.com`
- Point `cryptosentiment.it.com` to Beanstalk (A record or CNAME)
- Use in frontend: `https://cryptosentiment.it.com/api`

But this conflicts with your frontend domain. Using `api.cryptosentiment.it.com` is cleaner.

## Quick Summary

1. Delete failed certificate
2. Request certificate for `api.cryptosentiment.it.com`
3. Add validation CNAME to DNS
4. Add CNAME pointing `api` → Beanstalk
5. Add HTTPS listener in Beanstalk
6. Update frontend to use `https://api.cryptosentiment.it.com/api`


