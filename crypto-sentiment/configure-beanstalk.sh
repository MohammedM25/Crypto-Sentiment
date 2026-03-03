#!/bin/bash
# Configure Beanstalk Environment
# Run this when your environment is "Ready"

ENV_NAME="Crypto-Sentiment-Analysis-Projec-env"
APP_NAME="Crypto-Sentiment-Analysis-Project"
REGION="us-east-2"

echo "🚀 Configuring Beanstalk Environment: $ENV_NAME"
echo ""

# Check if environment is Ready
STATUS=$(aws elasticbeanstalk describe-environments --environment-names "$ENV_NAME" --region "$REGION" --query 'Environments[0].Status' --output text)
if [ "$STATUS" != "Ready" ]; then
  echo "⚠️  Environment is not Ready (Status: $STATUS)"
  echo "   Wait for it to be Ready, then run this script again."
  exit 1
fi

echo "✅ Environment is Ready!"
echo ""

# Step 1: Add Environment Variables
echo "Step 1: Adding environment variables..."
aws elasticbeanstalk update-environment \
  --application-name "$APP_NAME" \
  --environment-name "$ENV_NAME" \
  --region "$REGION" \
  --option-settings \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=SPRING_PROFILES_ACTIVE,Value=prod \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=PORT,Value=5000 \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=RDS_HOSTNAME,Value=crypto-sentiment-db.crusa8weoxnc.us-east-2.rds.amazonaws.com \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=RDS_PORT,Value=5432 \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=RDS_DB_NAME,Value=crypto_sentiment \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=RDS_USERNAME,Value=dbadmin \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=RDS_PASSWORD,Value=CryptoPassword123! \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=AWS_REGION,Value=us-east-2 \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=AWS_SQS_PROCESSING_QUEUE_URL,Value=https://sqs.us-east-2.amazonaws.com/203129030113/crypto-sentiment-processing-queue \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=AWS_S3_BUCKET_NAME,Value=crypto-sentiment-data-mohammed-2025 \
  --query 'Environment.[EnvironmentName,Status]' --output text

echo ""
echo "⚠️  Note: Add AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY manually in console"
echo "   (for security reasons)"
echo ""

# Step 2: Check/Attach IAM Instance Profile
echo "Step 2: Configuring IAM instance profile..."
INSTANCE_PROFILE=$(aws elasticbeanstalk describe-environment-resources --environment-name "$ENV_NAME" --region "$REGION" --query 'EnvironmentResources.InstanceProfiles[0]' --output text 2>&1)

if [ "$INSTANCE_PROFILE" = "None" ] || [ -z "$INSTANCE_PROFILE" ]; then
  echo "Creating IAM role and instance profile..."
  ROLE_NAME="crypto-sentiment-eb-role"
  PROFILE_NAME="crypto-sentiment-eb-profile"
  
  # Create role (if doesn't exist)
  aws iam create-role --role-name "$ROLE_NAME" \
    --assume-role-policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"ec2.amazonaws.com"},"Action":"sts:AssumeRole"}]}' 2>/dev/null || echo "Role already exists"
  
  # Create instance profile (if doesn't exist)
  aws iam create-instance-profile --instance-profile-name "$PROFILE_NAME" 2>/dev/null || echo "Profile already exists"
  
  # Add role to profile (if not already added)
  aws iam add-role-to-instance-profile --instance-profile-name "$PROFILE_NAME" --role-name "$ROLE_NAME" 2>/dev/null || echo "Role already in profile"
  
  # Attach policies
  echo "Attaching IAM policies..."
  aws iam attach-role-policy --role-name "$ROLE_NAME" --policy-arn arn:aws:iam::aws:policy/AmazonSQSFullAccess 2>/dev/null
  aws iam attach-role-policy --role-name "$ROLE_NAME" --policy-arn arn:aws:iam::aws:policy/AmazonS3FullAccess 2>/dev/null
  aws iam attach-role-policy --role-name "$ROLE_NAME" --policy-arn arn:aws:iam::aws:policy/AWSElasticBeanstalkWebTier 2>/dev/null
  aws iam attach-role-policy --role-name "$ROLE_NAME" --policy-arn arn:aws:iam::aws:policy/AWSElasticBeanstalkWorkerTier 2>/dev/null
  
  echo "Attaching instance profile to environment..."
  aws elasticbeanstalk update-environment \
    --application-name "$APP_NAME" \
    --environment-name "$ENV_NAME" \
    --region "$REGION" \
    --option-settings Namespace=aws:autoscaling:launchconfiguration,OptionName=IamInstanceProfile,Value="$PROFILE_NAME" \
    --query 'Environment.[EnvironmentName,Status]' --output text
else
  echo "Instance profile exists: $INSTANCE_PROFILE"
  ROLE_NAME=$(aws iam get-instance-profile --instance-profile-name "$INSTANCE_PROFILE" --query 'InstanceProfile.Roles[0].RoleName' --output text 2>&1)
  echo "Role: $ROLE_NAME"
  echo "Checking policies..."
  aws iam list-attached-role-policies --role-name "$ROLE_NAME" --query 'AttachedPolicies[*].PolicyName' --output text
fi

echo ""
echo "✅ Configuration complete!"
echo ""
echo "Next steps:"
echo "  1. Add AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY in Beanstalk console"
echo "  2. Build and deploy your JAR file"
echo "  3. Test the endpoint"


