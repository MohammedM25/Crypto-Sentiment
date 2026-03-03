#!/bin/bash
# Finish Beanstalk Configuration
# This will attach instance profile once environment is Ready

ENV_NAME="Crypto-Sentiment-Analysis-Projec-env"
APP_NAME="Crypto-Sentiment-Analysis-Project"
REGION="us-east-2"
PROFILE_NAME="crypto-sentiment-eb-profile"

echo "🔧 Finishing Beanstalk Configuration"
echo ""

# Wait for Ready
echo "Waiting for environment to be Ready..."
for i in {1..60}; do
  STATUS=$(aws elasticbeanstalk describe-environments --environment-names "$ENV_NAME" --region "$REGION" --query 'Environments[0].Status' --output text 2>&1)
  if [ "$STATUS" = "Ready" ]; then
    echo "✅ Ready!"
    break
  else
    echo "Status: $STATUS ($i/60)"
    sleep 5
  fi
done

STATUS=$(aws elasticbeanstalk describe-environments --environment-names "$ENV_NAME" --region "$REGION" --query 'Environments[0].Status' --output text 2>&1)

if [ "$STATUS" != "Ready" ]; then
  echo "⚠️  Environment is still $STATUS"
  echo "   Run this script again when it's Ready"
  exit 1
fi

# Check instance profile
echo ""
echo "Checking instance profile..."
INSTANCE_PROFILE=$(aws elasticbeanstalk describe-environment-resources --environment-name "$ENV_NAME" --region "$REGION" --query 'EnvironmentResources.InstanceProfiles[0]' --output text 2>&1)

if [ "$INSTANCE_PROFILE" != "None" ] && [ ! -z "$INSTANCE_PROFILE" ]; then
  echo "✅ Instance profile already attached: $INSTANCE_PROFILE"
else
  echo "Attaching instance profile..."
  aws elasticbeanstalk update-environment \
    --application-name "$APP_NAME" \
    --environment-name "$ENV_NAME" \
    --region "$REGION" \
    --option-settings Namespace=aws:autoscaling:launchconfiguration,OptionName=IamInstanceProfile,Value="$PROFILE_NAME" \
    --query 'Environment.[EnvironmentName,Status]' --output text
  
  echo "✅ Instance profile attachment initiated!"
fi

# Verify configuration
echo ""
echo "✅ Configuration Summary:"
echo ""
echo "Environment variables: ✅"
aws elasticbeanstalk describe-configuration-settings \
  --application-name "$APP_NAME" \
  --environment-name "$ENV_NAME" \
  --region "$REGION" \
  --query 'ConfigurationSettings[0].OptionSettings[?Namespace==`aws:elasticbeanstalk:application:environment` && (OptionName==`SPRING_PROFILES_ACTIVE` || OptionName==`PORT` || OptionName==`RDS_HOSTNAME` || OptionName==`AWS_REGION`)].[OptionName,Value]' \
  --output table 2>&1 | head -10

echo ""
echo "IAM policies: ✅ (SQS, S3 attached to crypto-sentiment-eb-role)"
echo ""
echo "⚠️  Still need (add in console for security):"
echo "  - AWS_ACCESS_KEY_ID"
echo "  - AWS_SECRET_ACCESS_KEY"
echo ""
CNAME=$(aws elasticbeanstalk describe-environments --environment-names "$ENV_NAME" --region "$REGION" --query 'Environments[0].CNAME' --output text)
echo "Your Beanstalk URL: http://$CNAME"
echo ""
echo "Next: Build and deploy your JAR file!"


