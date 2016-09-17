# Beanstalk Workshop

Slides from the presentation are [here](https://helleroy.github.io/beanstalk-workshop)

# Prerequisites

- Ensure you have [awsebcli](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-welcome.html) installed, otherwise install it with ```brew install awsebcli``` or ```pip install awsebcli```. 
You can verify that you have it installed by typing ```eb```
- Ensure you have [awscli](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb-cli3.html) installed, otherwise install it with ```brew install awscli``` or ```pip install awscli```. 
You can verify that you have it installed by typing ```aws```
- Get the AWS Access key and secret key for [your account](https://console.aws.amazon.com/console/home) and use them in your terminal with.
```
export AWS_ACCESS_KEY_ID=C99F5C7EE00F1EXAMPLE
export AWS_SECRET_ACCESS_KEY=a63xWEj9ZFbigxqA7wI3Nuwj3mte3RDBdEXAMPLE
```
- Export the region you're working in (See short names [here](http://docs.aws.amazon.com/general/latest/gr/rande.html)). 
*(Note: not all commands in the CLI will use this)*
```
export AWS_DEFAULT_REGION=eu-central-1
```
- Create an ssh-key for ssh access to our instances
```
ssh-keygen -f ~/.ssh/beanstalkworkshop
```
- Clone and enter this repo
```
git clone git@github.com:helleroy/beanstalk-workshop.git
cd beanstalk-workshop
```

## Step 1. Set up Beanstalk

### 1.1 Initialize
- Using [init](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-init.html), initialize beanstalk on this folder with java-8 as platform. Use the name of the ssh-key you created previously.
```
eb init --region eu-central-1 --keyname beanstalkworkshop --platform java-8
```

### 1.2 Configure
- Using [create](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-create.html), configure the Beanstalk application to use 2 x t2.micro EC2 instances with a Load Balancer in front. Remember to specify the ssh-key you created previously so you can ssh into our instances.
```
eb create BeanstalkWorkshopApp --region eu-central-1 --instance_type t2.micro --keyname beanstalkworkshop --platform java-8 --scale 2
```

This will take ~5 minutes. AWS will create loadbalancer, EC2-instances, CloudWatch alarms, security groups and S3 bucket for the environment data. 

You can look around the console to watch it in action. You can open the console using ```eb console```

*(Note: If the command above fails due to missing default vpc or missing default subnet, this means you have been playing around too much in AWS. You can then go back to Step 1.1, and do the workshop with another region. Alternatively, see [create](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-create.html) on how to specify a vpc, subnet etc.)*

### 1.3 See it
Voilà! Your app is running! 

- You can open it with ```eb open``` - it should respond with ```pong```. You can also go to ```/hostname``` to see which EC2 instance is responding. 
- Refresh the page and watch it alternate between your two instances. You can also see the health status of your app with ```eb health```.

## Step 2. Change app and deploy

**Procfile**
The Procfile describes what Beanstalk should invoke to launch a Java app. You can use this in cases where you have multiple .jar files or need to configure the JVM in some way.
Without a Procfile Beanstalk will by default invoke ```java -jar <your-app-name>.jar```.

**Buildfile**
The Buildfile describes how Beanstalk should build and package your code when deploying. Beanstalk's Java image comes with standard build tools to enable you to build on-server (Maven, Gradle etc.)
More often than not you will probably be using a build server for this, but the option is there.

### 2.1 Change the app text
- Change the response text from the app at ```Endpoint.java```. For example, at the method ```ping```, replace ```"pong"``` with your own reply. 

- Also, change the method ```error``` to log your name in System.err.println. (We'll print this to Slack at the end of the workshop).

### 2.2 Deploy the changes
- Commit the changes and, using [deploy](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-deploy.html), deploy your changes to aws.
```
git commit -am "Change response text"
eb deploy
```

During the deploy you will see that EB deploys to one instance at a time. If you refresh the webpage while it deploys you can observe both versions of your application.

## Step 3. Configure auto scaling

You will use ```eb config```for the following steps. This command will open the current environment configuration for your EB application in a text editor (Nano by default).
It may not work properly on some machines; we've had trouble getting it to work on OS X when the home folder has whitespaces in it, for instance.
Because of this there's a workaround where you can use the AWS CLI to do the same steps. 

### 3.1 Set auto scaling minimum and maximum
When you specified ```--scale 2``` in 1.2, you set both the minimum and maximum amount of instances the Load Balancer should spin up. 

In order for the app to scale dynamically, you can use either of the following:

- EB CLI:
Use ```eb config```, locate the following section in the configuration, and change it like so:
```
aws:autoscaling:asg:
    Availability Zones: Any
    Cooldown: '360'
    Custom Availability Zones: ''
    MaxSize: '4'
    MinSize: '2'
```

Then save and close the file. EB will automatically deploy the new environment configuration.

- AWS CLI:
**If ```eb config``` didn't work**, invoke the following using ```aws```
```
aws autoscaling update-auto-scaling-group
    --region eu-central-1
    --auto-scaling-group-name "<my-auto-scaling-group>"
    --min-size 2
    --max-size 4
```

### 3.2 Set metrics to trigger alarm
The alarms that will scale your application are based on network traffic by default. You can see your current alarms in the CloudWatch part of the AWS Console.
Change these to instead be based on the number of requests by using either of the following:

- EB CLI:
Use ```eb config```, locate the following section in the configuration, and change it like so:
```
  AWSEBCloudwatchAlarmHigh.aws:autoscaling:trigger:
    UpperThreshold: '5'
  AWSEBCloudwatchAlarmLow.aws:autoscaling:trigger:
    BreachDuration: '5'
    EvaluationPeriods: '1'
    LowerThreshold: '2'
    MeasureName: RequestCount
    Period: '1'
    Statistic: Average
    Unit: Count
```

Then save and close the file. EB will automatically deploy the new environment configuration.

- AWS CLI:
**If ```eb config``` didn't work**, invoke the following using ```aws```
```
aws cloudwatch put-metric-alarm
    --alarm-name "<my-existing-AWSEBCloudwatchAlarmHigh-alarm>"
    --metric-name RequestCount
    --namespace AWS/ELB
    --statistic Average
    --period 30
    --evaluation-periods 60
    --threshold 5
    --comparison-operator GreaterThanThreshold
 
aws cloudwatch put-metric-alarm
    --alarm-name "<my-existing-AWSEBCloudwatchAlarmLow-alarm>"
    --metric-name RequestCount
    --namespace AWS/ELB
    --statistic Average
    --period 30
    --evaluation-periods 60
    --threshold 2
    --comparison-operator LessThanThreshold
```

### 3.3 See the app auto-scale
Send several requests to your webapp to get over the threshold you've created for scaling up. 

- You can use the command below will request your website once every 5 second, and write the IP of the EC2 instance responding.
```
while sleep 5; do curl <dns-name-for-my-load-balancer>/hostname; done;
``` 

*(Note: Your DNS name can be found with ```eb open```)*

After a couple of minutes, a new instance will be spun up, and respond with a new IP for your requests.

## Step 4. Log output
EB will store a few logs by default. You can download them all using
```
eb logs --all
```

Your **error logs** will be stored in ```/var/log/web-1.error.log```
and your **access logs** will by default be stored at ```/var/log/web-1.log```

In order to get better control over our logs, we'll send them to AWS Cloudwatch, and from there we want the error logs to appear in Slack. 

### Step 4.1 Add logs to Cloudwatch
We've taken the liberty of adding a cloudwatch extension to this beanstalk (see ```.ebextensions/cloudwatchlogs-nginx```). This is the unchanged [EB extension for nginx CloudWatch](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/AWSHowTo.cloudwatchlogs.html#AWSHowTo.cloudwatchlogs.files).

In order for our instances to be allowed to put logs on Cloudwatch, we must add ```CloudWatchLogsFullAccess``` policy to the instance profile role. This role is by default ```aws-elasticbeanstalk-ec2-role```, and can be verified with ```eb config``` (find IamInstanceProfile)

- Grant your instances access to log to CloudWatch
```
aws iam attach-role-policy --policy-arn arn:aws:iam::aws:policy/CloudWatchLogsFullAccess --role-name aws-elasticbeanstalk-ec2-role
```

Verify that logs appear in Cloudwatch by going to AWS CloudWatch > Logs. These will be named after your instance id, and show logs from nginx. 


### Step 4.2 Add application logs to CloudWatch
By default, the logs that are named after your instance id, and they only show logs from nginx. If we want to show logs from the application (i.e. ```System.out.println``` and ```System.err.println```), we'll have to specify them in ```.ebextensions/cloudwatchlogs-nginx/cwl-webrequest-metrics.config```.


#### Step 4.2.1 Create new log groups 
We'll create one log group for access logs, and one for error logs

- Below ```Resources:```, insert the following:
```
AWSEBCloudWatchLogs8832c8d3f1a54c238a40e36f31ef55a0ApplicationErrorLog: 
  Type: "AWS::Logs::LogGroup"
  DependsOn: AWSEBBeanstalkMetadata
  DeletionPolicy: Retain
  Properties:
    LogGroupName: 
      "Fn::GetOptionSetting":
        Namespace: "aws:elasticbeanstalk:application:environment"
        OptionName: WebRequestCWLogGroup
        DefaultValue: {"Fn::Join":["-", [{ "Ref":"AWSEBEnvironmentName" }, "application-error"]]}
    RetentionInDays: 14

AWSEBCloudWatchLogs8832c8d3f1a54c238a40e36f31ef55a0ApplicationAccessLog: 
  Type: "AWS::Logs::LogGroup"
  DependsOn: AWSEBBeanstalkMetadata
  DeletionPolicy: Retain
  Properties:
    LogGroupName: 
      "Fn::GetOptionSetting":
        Namespace: "aws:elasticbeanstalk:application:environment"
        OptionName: WebRequestCWLogGroup
        DefaultValue: {"Fn::Join":["-", [{ "Ref":"AWSEBEnvironmentName" }, "application-access"]]}
    RetentionInDays: 14
```

#### Step 4.2.1 Add logs to the new log groups 
- Find ```files:``` (below ```CWLogsAgentConfigSetup:```) and insert the following:
```
"/tmp/cwlogs/conf.d/web-access.conf":
  content : |
    [web-access_log]
    file = /var/log/web-1.log
    log_group_name = `{ "Ref" : "AWSEBCloudWatchLogs8832c8d3f1a54c238a40e36f31ef55a0WebRequestLogGroup" }`
    log_stream_name = {instance_id}
  mode  : "000400"
  owner : root
  group : root

"/tmp/cwlogs/conf.d/web-error.conf":
  content : |
    [web-error_log]
    file = /var/log/web-1.error.log
    log_group_name = `{ "Ref" : "AWSEBCloudWatchLogs8832c8d3f1a54c238a40e36f31ef55a0ApplicationErrorLog" }`
    log_stream_name = {instance_id}
  mode  : "000400"
  owner : root
  group : root
```

### Step 4.3 Commit and verify the changes

- Commit and deploy the changes with:
```
git add .ebextensions
git commit -m "Add application logging"
eb deploy
```

Go to ```/500``` and ```/hostname``` on your server, then verify that the logs have appeared in [AWS Console](https://console.aws.amazon.com/console/home?region=eu-central-1), under CloudWatch > Logs > BeanstalkWorkshopApp-application-error.

## Step 5. Stream log outputs to Lambda

Now that our logs are in CloudWatch, we can stream them to Slack using a Lambda function.

### Step 5.1 Create a Lamba function
- From the AWS Console, go to Lambda.
- Find and select ```cloudwatch-log-to-loggly```.
- Name the filter, but leave the pattern blank
- Use log group ```BeanstalkWorkshopApp-application-error```
- Check ```Enable trigger``` and click Next. 

### Step 5.2 Replace Lambda code
On the next screen we'll replace the code so it logs to Slack instead of Loggly.

- Name your Lambda function and select Runtime ```Node.js 4.3```.
- Under Lambda function code, select Code Entry type ```Edit code inline```, and replace the code with the code from [this Github Gist](https://gist.github.com/tomfa/f4e090cbaff0189eba17c0fc301c63db#file-cwlogsslack-js).
- At the top of the inserted code, set ```UNENCRYPTED_URL``` to ```'/services/T0FHGDP0T/B2BEAUMGQ/BXwRUZ1ZuW8hz61cWtkrclpN'```. *(This is the [Incoming webhook](https://api.slack.com/incoming-webhooks) of a Slack channel we've set up for this workshop)*
- At the top of the inserted code, set ```CHANNEL``` to ```'#beanstalk-workshop'```
- Under ```Lambda function handler and role```, you can leave all the options as default. Just remember to name your role
- Click Next

Voilà! You're done! Your error logs should now appear in our Slack channel on the projector :)

## Step 6. Destroy your app
Clean up after yourself, destroying everything related to this app with
```
eb terminate -–all
```

## Reference: Create everything with Terraform
TODO: Reference some terraform files that will setup the same things we just did (No need for them to do this?)


