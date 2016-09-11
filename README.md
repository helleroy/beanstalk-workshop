# Beanstalk Workshop

Slides from the presentation can be found [here](https://helleroy.github.io/beanstalk-workshop)

# Prerequisites

- Ensure you have [awsebcli](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb-cli3.html) installed, otherwise install it with ```brew install awsebcli``` or ```pip install awsebcli```
- Get the AWS Access key and secret key for [your account](https://console.aws.amazon.com/console/home) and use them in your terminal with.
```
export AWS_ACCESS_KEY_ID=C99F5C7EE00F1EXAMPLE
export AWS_SECRET_ACCESS_KEY=a63xWEj9ZFbigxqA7wI3Nuwj3mte3RDBdEXAMPLE
```
- Export the region you're working in with (See short names [here](http://docs.aws.amazon.com/general/latest/gr/rande.html))
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
Using [init](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-init.html), initialize beanstalk on this folder, with java-8 as platform. Use the ssh-key created in the previous step
```
eb init --keyname beanstalkworkshop --platform java-8
```

### 1.2 Configure
Using [create](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-create.html), configure the Beanstalk application to use 2 x t2.micro EC2 instances with a Load Balancer in front. Remember to again specify the previously created ssh-key as key for ssh'ing into instances with.
```
eb create BeanstalkWorkshopApp
    --instance_type t2.micro
    --keyname beanstalkworkshop
    --platform java-8
    --scale 2
```
This will take ~5 minutes. AWS will create loadbalancer, EC2-instances, CloudWatch alarms, security groups and S3 bucket for the environment data. You can look around the console to watch it in action.

### 1.3 See it
Voilla! Your app is up! Go to your Load Balancer section on your EC2 console, and open the DNS in a new tab. It should respond "pong". Also, you could go to ```/hostname``` to see which EC2 instance is responding. Refresh the page, and watch it alternate between your two instances.


## Step 2. Change app and deploy
### 2.1 Procfile
[TODO: Explain the Procfile, make a change]

### 2.2 Buildfile
[TODO: Explain the Buildfile, make a change]

### 2.3 Change the app text
Change the response text from the app at ```Endpoint.java```. For example, at the method ```ping```, replace ```"pong"``` with your own reply.

### 2.4 Deploy the changes
Commit the changes and, using [deploy](http://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-deploy.html), deploy your changes to aws.
```
git commit -am "Change response text"
eb deploy
```
You will during the deploy, see that EB deploys on one instance at the time. If you refresh the webpage while it deploys, you can observe both versions of your application.

## Step 3. Demonstrate autoscaling

## Step 4. Create log output

## Step 5. Destroy your app

## Step 6. Deploy everything with Terraform


