# Deploy Frontend to Vercel

## Quick Steps

### Option 1: Using Vercel Dashboard (Recommended)

1. **Go to Vercel Dashboard**
   - Visit: https://vercel.com/dashboard
   - Sign in to your account

2. **Import/Select Your Project**
   - If project exists: Click on `crypto-sentiment` (or your project name)
   - If new project: Click "Add New" → "Project" → Import from Git (GitHub/GitLab)

3. **Add Environment Variable**
   - Go to: **Settings** → **Environment Variables**
   - Click **Add New**
   - **Name**: `REACT_APP_API_URL`
   - **Value**: `http://cryptosent.us-east-2.elasticbeanstalk.com/api`
   - **Environment**: Select all (Production, Preview, Development)
   - Click **Save**

4. **Deploy**
   - Go to: **Deployments** tab
   - Click **Redeploy** on latest deployment
   - OR push to your Git repository (if connected)

### Option 2: Using Vercel CLI

1. **Install Vercel CLI** (if not installed):
   ```bash
   npm install -g vercel
   ```

2. **Login to Vercel**:
   ```bash
   vercel login
   ```

3. **Navigate to frontend directory**:
   ```bash
   cd frontend
   ```

4. **Add environment variable**:
   ```bash
   vercel env add REACT_APP_API_URL production
   # When prompted, enter: http://cryptosent.us-east-2.elasticbeanstalk.com/api
   ```

5. **Deploy**:
   ```bash
   vercel --prod
   ```

### Option 3: Connect Git Repository (Automatic Deploys)

1. **Connect Repository**:
   - In Vercel Dashboard → Add New Project
   - Connect your GitHub/GitLab repository
   - Select the `crypto-sentiment/frontend` directory as root

2. **Add Environment Variable**:
   - Settings → Environment Variables
   - Add `REACT_APP_API_URL` = `http://cryptosent.us-east-2.elasticbeanstalk.com/api`

3. **Deploy**:
   - Vercel will automatically deploy on every push to main branch
   - Or manually trigger from dashboard

## Verify Deployment

1. **Check Your Domain**:
   - Visit: `https://cryptosentiment.it.com`
   - Should load the frontend

2. **Check Browser Console**:
   - Open DevTools (F12)
   - Go to Console tab
   - Should see API calls to: `http://cryptosent.us-east-2.elasticbeanstalk.com/api`
   - No CORS errors

3. **Test API Connection**:
   - Open Network tab in DevTools
   - Refresh page
   - Look for requests to `/api/trends/summary`, `/api/market/price`, etc.
   - Should return 200 OK

## Troubleshooting

**Frontend shows "Connection Error":**
- Check environment variable is set correctly
- Verify backend is running: `curl http://cryptosent.us-east-2.elasticbeanstalk.com/health`
- Check browser console for CORS errors

**CORS Errors:**
- Backend CORS should allow `https://cryptosentiment.it.com`
- Rebuild and redeploy backend if needed

**Environment Variable Not Working:**
- Make sure variable name is exactly: `REACT_APP_API_URL`
- Redeploy after adding environment variable
- Check Vercel build logs for errors

## Quick Reference

**Backend URL**: `http://cryptosent.us-east-2.elasticbeanstalk.com/api`  
**Frontend Domain**: `https://cryptosentiment.it.com`  
**Environment Variable**: `REACT_APP_API_URL`

