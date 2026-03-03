# DNS Validation for Certificate

## Current Status
- Certificate ID: `79eea907-0621-4bd8-bad5-9d573acb7681`
- Domain: `api.cryptosentiment.it.com`
- Status: **Pending validation**

## CNAME Record to Add

**CNAME Name:** `_062b636bb2f23fabeda43bebaf293658.api.cryptosentiment.it.com`

**CNAME Value:** (Check the certificate page - should be like `_xyz789.acm-validations.aws.`)

## Steps to Validate

### Option 1: If Using Route 53 (Easiest)

1. On the certificate page, click **"Create records in Route 53"** button
2. AWS will automatically create the CNAME record
3. Wait 5-10 minutes for validation

### Option 2: Manual DNS Entry

1. **Go to your DNS provider** (NS1, Cloudflare, GoDaddy, etc.)
2. **Find where you manage** `cryptosentiment.it.com` DNS records
3. **Add CNAME record:**
   - **Type**: CNAME
   - **Name/Host**: `_062b636bb2f23fabeda43bebaf293658.api`
     - (Some providers need just `_062b636bb2f23fabeda43bebaf293658.api`)
     - (Others need full `_062b636bb2f23fabeda43bebaf293658.api.cryptosentiment.it.com`)
   - **Value/Target**: (The CNAME value from AWS - check certificate page)
   - **TTL**: 3600 (or default)

4. **Save the record**
5. **Wait 5-10 minutes** for DNS propagation
6. **Check certificate status** - should change to "Issued"

## How to Find CNAME Value

On the certificate page:
- Look at the "Domains" table
- Find the "CNAME value" column (might be hidden - expand table)
- OR click on the domain row to see details
- OR export to CSV to see all values

## After Validation

Once certificate shows "Issued":
1. Point `api.cryptosentiment.it.com` to Beanstalk (CNAME: `api` → `cryptosent.us-east-2.elasticbeanstalk.com`)
2. Add HTTPS listener in Beanstalk
3. Update frontend URL

## Troubleshooting

**Validation taking too long:**
- Check DNS record is correct
- Wait up to 30 minutes (usually 5-10)
- Verify CNAME record appears in DNS lookup

**Can't find CNAME value:**
- Click on the domain row in the table
- Or export to CSV
- Or check AWS documentation


