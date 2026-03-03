# Custom Domain Setup for cryptosentiment.it.com

## ✅ What's Already Done

1. **Vercel Proxy Configured** (`vercel.json`)
   - Routes `/api/*` requests to your HTTP backend
   - Avoids mixed content (HTTPS → HTTP) issues

2. **Frontend Updated**
   - Uses relative `/api` URLs
   - Works with the Vercel proxy

## 🔧 Connect Your Domain

### Step 1: Add Domain in Vercel

**Via Dashboard (Recommended):**
1. Go to: https://vercel.com/dashboard
2. Select your project (`frontend` or your project name)
3. Go to: **Settings** → **Domains**
4. Click **Add Domain**
5. Enter: `cryptosentiment.it.com`
6. Click **Add**

**Via CLI:**
```bash
cd frontend
npx vercel domains add cryptosentiment.it.com
```

### Step 2: Update DNS Records

Vercel will show you DNS records to add. Typically:

**If using NS1 (or your DNS provider):**

**Option A: CNAME Record (Recommended)**
- **Type**: CNAME
- **Name**: `@` or `cryptosentiment.it.com`
- **Value**: `c-*.vercel-dns.com` (Vercel will provide exact value)
- **TTL**: 3600

**Option B: A Record**
- **Type**: A
- **Name**: `@` or `cryptosentiment.it.com`
- **Value**: Vercel IP addresses (Vercel will provide)
- **TTL**: 3600

### Step 3: Wait for DNS Propagation

- DNS changes can take 5 minutes to 48 hours
- Usually takes 5-15 minutes
- Check status in Vercel dashboard

### Step 4: Verify Domain

1. Go to Vercel Dashboard → Your Project → Domains
2. Wait for status to show "Valid Configuration"
3. Visit: `https://cryptosentiment.it.com`
4. Should load your frontend!

## 🧪 Test After Setup

1. **Visit**: `https://cryptosentiment.it.com`
2. **Open DevTools** (F12) → Console
3. **Check**: Should see API calls to `/api/trends/summary`
4. **Verify**: Data loads from backend

## 🔍 Troubleshooting

**Domain not working:**
- Check DNS records are correct
- Wait for DNS propagation (can take up to 48 hours)
- Verify domain in Vercel dashboard shows "Valid Configuration"

**Still getting connection errors:**
- Check Vercel proxy is working: Visit `https://cryptosentiment.it.com/api/trends/summary`
- Should return JSON data
- If not, check `vercel.json` is in your project

**Mixed content warnings:**
- Shouldn't happen with Vercel proxy
- All requests go through Vercel's HTTPS

## 📋 Quick Reference

**Frontend Domain**: `https://cryptosentiment.it.com`  
**Backend URL**: `http://cryptosent.us-east-2.elasticbeanstalk.com`  
**Proxy**: Vercel routes `/api/*` → Backend  
**Status**: Domain needs to be connected in Vercel


