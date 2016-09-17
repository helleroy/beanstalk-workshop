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
Note: not all commands in the CLI will use this.
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
Using [init](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-init.html), initialize beanstalk on this folder with java-8 as platform. Use the name of the ssh-key you created previously.
```
eb init --region eu-central-1 --keyname beanstalkworkshop --platform java-8
```

### 1.2 Configure
Using [create](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-create.html), configure the Beanstalk application to use 2 x t2.micro EC2 instances with a Load Balancer in front. 
Remember to specify the ssh-key you created previously so you can ssh into our instances.
```
eb create BeanstalkWorkshopApp --region eu-central-1 --instance_type t2.micro --keyname beanstalkworkshop --platform java-8 --scale 2
```

This will take ~5 minutes. AWS will create loadbalancer, EC2-instances, CloudWatch alarms, security groups and S3 bucket for the environment data. 
You can look around the console to watch it in action. You can open the console using ```eb console```

Note: If the command above fails due to missing default vpc or missing default subnet , you can remove your steps with ```eb --terminate```. Then, add the following to the previous command _(replace vpc id's and subnet id's with those you wish to place infrastructure in)_
```
--vpc.id vpc-64b1870d 
--vpc.ec2subnets subnet-50866a2a,subnet-c007d8a8 
```

### 1.3 See it
Voilà! Your app is running! You can open it with ```eb open``` - it should respond with some text. You can also go to ```/hostname``` to see which EC2 instance is responding. 
Refresh the page and watch it alternate between your two instances. You can also see the health status of your app with ```eb health```.

## Step 2. Change app and deploy

**Procfile**
The Procfile describes what Beanstalk should invoke to launch a Java app. You can use this in cases where you have multiple .jar files or need to configure the JVM in some way.
Without a Procfile Beanstalk will by default invoke ```java -jar <your-app-name>.jar```.

**Buildfile**
The Buildfile describes how Beanstalk should build and package your code when deploying. Beanstalk's Java image comes with standard build tools to enable you to build on-server (Maven, Gradle etc.)
More often than not you will probably be using a build server for this, but the option is there.

### 2.1 Change the app text
Change the response text from the app at ```Endpoint.java```. For example, at the method ```ping```, replace ```"pong"``` with your own reply. 
Also, change the method ```error``` to log your name in System.err.println (We're going to use it for a later step).

### 2.2 Deploy the changes
Commit the changes and, using [deploy](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-deploy.html), deploy your changes to aws.
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
Invoke the following using ```aws```
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
Invoke the following using ```aws```
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
You can use the command below will request your website once every 5 second, and write the IP of the EC2 instance responding.
```
while sleep 5; do curl <dns-name-for-my-load-balancer>/hostname; done;
``` 

*Your DNS name can be found with ```eb open```*

After a couple of minutes, a new instance will be spun up, and respond with a new IP for your requests.

## Step 4. Log output
EB will store a few logs by default. You can download them all using
```
eb logs --all
```

In order to get better control over our logs, we'll send them to AWS Cloudwatch. In order to do this we'll have to run a setup of awslogs-agent on our instances.

### Step 4.1 Add logs to Cloudwatch
TODO: Install ```https://s3.amazonaws.com.aws-cloudwatch/downloads/awslogs-agent-setup-v1.0.py``` on startup. This could be done in step 2.1? Help can be found [here](http://notes.webutvikling.org/aws-send-ec2-logs-to-slack/)

[TODO: Add logs from your apps to Cloudwatch.]

## Step 5. Stream log outputs to Lambda

Help for these step can be found [here](http://notes.webutvikling.org/aws-send-ec2-logs-to-slack/).

### Step 5.1 Send logs to a new SNS Topic
[TODO: Create SNS and send logs there]

### Step 5.2 Create Lambda function that sends to Slack
[TODO: Add your logs to Slack]

### Step 5.3 Add Lambda as subscriber to SNS Topic
[TODO: Add our new Lambda function to SNS Topic]

## Step 6. Destroy your app
Clean up after yourself, destroying everything related to this app with
```
eb terminate -–all
```

## Reference: Create everything with Terraform
TODO: Reference some terraform files that will setup the same things we just did (No need for them to do this?)


