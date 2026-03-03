# Add CNAME Record in NS1

## Your Current Setup
- Domain: `cryptosentiment.it.com`
- Nameservers: NS1 (`dns1.p08.nsone.net`, etc.)
- You need to add CNAME in **NS1**, not Namecheap

## Steps to Add CNAME in NS1

### Step 1: Go to NS1
1. Go to: https://portal.ns1.com
2. Log in to your NS1 account
3. If you don't have an account, you may need to create one or check if Namecheap created it for you

### Step 2: Find Your Domain
1. In NS1 dashboard, find `cryptosentiment.it.com`
2. Click on it to view DNS records

### Step 3: Add CNAME Record
1. Click **"Add Record"** or **"+"** button
2. Select **"CNAME"** as record type
3. Fill in:
   - **Name/Host**: `_062b636bb2f23fabeda43bebaf293658.api`
     - (Just the subdomain part, NS1 will add the domain)
   - **Target/Value**: (The CNAME value from AWS - check certificate page)
     - Should look like: `_xyz789.acm-validations.aws.`
   - **TTL**: 3600 (or default)
4. Click **Save** or **Add Record**

### Step 4: Wait for Validation
- Wait 5-10 minutes
- Check AWS Certificate Manager - status should change to "Issued"

## Alternative: Switch to Namecheap BasicDNS

If you prefer to manage DNS in Namecheap:

1. **In Namecheap Advanced DNS page:**
   - Click **"Change DNS Type"** link (under HOST RECORDS section)
   - Switch to **"Namecheap BasicDNS"**
   - Wait for DNS propagation (can take up to 48 hours)

2. **Then add CNAME in Namecheap:**
   - Go to Advanced DNS tab
   - Click **"Add New Record"**
   - Type: **CNAME**
   - Host: `_062b636bb2f23fabeda43bebaf293658.api`
   - Value: (CNAME value from AWS)
   - TTL: Automatic
   - Click **Save**

**Note:** Switching DNS can take 24-48 hours, so using NS1 is faster.

## Finding the CNAME Value

If you don't have the CNAME value yet:

1. Go back to AWS Certificate Manager
2. Click on your certificate
3. In the "Domains" table, look for "CNAME value" column
4. Or click on the domain row to see details
5. Copy the value (should be like `_xyz789.acm-validations.aws.`)

## Quick Reference

**CNAME Name**: `_062b636bb2f23fabeda43bebaf293658.api`  
**CNAME Value**: (From AWS certificate page)  
**Where to add**: NS1 portal (portal.ns1.com)  
**Domain**: cryptosentiment.it.com


