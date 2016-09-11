# Beanstalk Workshop

Slides from the presentation can be found [here](https://helleroy.github.io/beanstalk-workshop)

# Prerequisites

- Ensure you have [awsebcli](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-welcome.html) installed, otherwise install it with ```brew install awsebcli``` or ```pip install awsebcli```. You can verify that you have it installed by typing ```eb```
- Ensure you have [awscli](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb-cli3.html) installed, otherwise install it with ```brew install awscli``` or ```pip install awscli```. You can verify that you have it installed by typing ```aws```
- Get the AWS Access key and secret key for [your account](https://console.aws.amazon.com/console/home) and use them in your terminal with.
```
export AWS_ACCESS_KEY_ID=C99F5C7EE00F1EXAMPLE
export AWS_SECRET_ACCESS_KEY=a63xWEj9ZFbigxqA7wI3Nuwj3mte3RDBdEXAMPLE
```
- Export the region you're working in with (See short names [here](http://docs.aws.amazon.com/general/latest/gr/rande.html)). Note: not all commands will take this into consideration (?).
```
export AWS_DEFAULT_REGION=eu-central-1
```
- Clone and enter this repo
```
git clone git@github.com:helleroy/beanstalk-workshop.git
cd beanstalk-workshop
```
- Create ssh keys for us to ssh into the instances with.
```
ssh-keygen -f ~/.ssh/beanstalkworkshop
```

## Step 1. Set up Beanstalk

### 1.1 Initialize
Using [init](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-init.html), initialize beanstalk on this folder, with java-8 as platform. Use the name of the ssh-key created in the previous step
```
eb init
    --region eu-central-1
    --keyname beanstalkworkshop
    --platform java-8
```

### 1.2 Configure
Using [create](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-create.html), configure the Beanstalk application to use 2 x t2.micro EC2 instances with a Load Balancer in front. Remember to again specify the previously created ssh-key as key for ssh'ing into instances with.
```
eb create BeanstalkWorkshopApp
    --region eu-central-1
    --instance_type t2.micro
    --keyname beanstalkworkshop
    --platform java-8
    --scale 2
```

This will take ~5 minutes. AWS will create loadbalancer, EC2-instances, CloudWatch alarms, security groups and S3 bucket for the environment data. You can look around the console to watch it in action.

### 1.3 See it
Voilla! Your app is up! You can open it with ```eb open```. It should respond "pong". Also, you could go to ```/hostname``` to see which EC2 instance is responding. Refresh the page, and watch it alternate between your two instances. You can also see the health status of your app with ```eb health```

## Step 2. Change app and deploy
### 2.1 Procfile
[TODO: Explain the Procfile, make a change]

### 2.2 Buildfile
[TODO: Explain the Buildfile, make a change]

### 2.3 Change the app text
Change the response text from the app at ```Endpoint.java```. For example, at the method ```ping```, replace ```"pong"``` with your own reply. Also, change the method ```error``` to log your name in System.err.println (We're going to use it for a later step).

### 2.4 Deploy the changes
Commit the changes and, using [deploy](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-deploy.html), deploy your changes to aws.
```
git commit -am "Change response text"
eb deploy
```
You will during the deploy, see that EB deploys on one instance at the time. If you refresh the webpage while it deploys, you can observe both versions of your application.

## Step 3. Demonstrate autoscaling

### 3.1 Set auto scaling minimum and maximum
When you specified ```--scale 2``` in the 1.2, you set both the minimum and maximum amount of instances the loadbalancer should spin up. In order for the app to be dynamically, you can use aws cli:
```
aws autoscaling update-auto-scaling-group
    --region eu-central-1
    --auto-scaling-group-name "<my-auto-scaling-group>"
    --min-size 1
    --max-size 3
```

### 3.2 Set metrics to trigger alarm
By default, alarms that will scale your application are based on Network Traffic going in and out. Change these to instead be based on number of requests. You can see your current alarms in the CloudWatch part of AWS Console.
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
```

```
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
Send several requests to your webapp, to get over the threshold you've created for scaling up. You can use the command below will request your website once every 5 second, and write the IP of the EC2 instance responding.
```
while sleep 5; do curl <dns-name-for-my-load-balancer>/hostname; done;
```
After a couple of minutes, a new instance will be spun up, and respond with a new IP for your requests.

## Step 4. Log output
By default EB will store a few logs. You can download them all using
```
eb logs --all
```
[TODO: Add logs from your apps to Cloudwatch. Make your own name to be logged when someone requests the page]

## Step 5. Put log outputs to Slack
[TODO: Add your logs to Slack]

## Step 6. Destroy your app
Clean up after yourself, destroying everything related to this app with
```
eb terminate –all
```

## Reference: Create everything with Terraform


