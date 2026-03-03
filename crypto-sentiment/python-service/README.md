# Python Scraper Service

This is the Lambda function that scrapes cryptocurrency news and analyzes sentiment.

## Files

- `lambda_function.py` - Main scraper code
- `requirements.txt` - Python dependencies

## Local Development

For local testing, install dependencies in a virtual environment:

```bash
python3 -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
python lambda_function.py
```

## Lambda Deployment

To deploy to AWS Lambda:

```bash
# Install dependencies in a temporary directory
pip install -r requirements.txt -t .

# Create deployment package
zip -r lambda-deployment.zip . -x "*.pyc" "__pycache__/*" "*.dist-info/*" "*.egg-info/*"

# Update Lambda function
aws lambda update-function-code \
  --function-name crypto-sentiment-scraper \
  --zip-file fileb://lambda-deployment.zip \
  --region us-east-2
```

**Note**: Dependencies are NOT included in this repo. They should be installed when deploying to Lambda or when running locally.


