# Jenkins CI/CD Setup for Spring Boot Todo API on AWS ECS

This project includes:

- `Dockerfile` - creates a Java 17 container image for the Spring Boot API.
- `.dockerignore` - keeps unnecessary files out of Docker build context.
- `Jenkinsfile` - CI/CD pipeline that builds, pushes to ECR, and deploys to ECS.
- `aws/task-definition-template.json` - ECS Fargate task definition template.

## Pipeline Flow

1. Checkout source code from GitHub.
2. Run `mvn clean verify` to build and test the Java project.
3. Build Docker image.
4. Login to Amazon ECR.
5. Push image to ECR.
6. Register a new ECS task definition revision.
7. Update ECS service to use the new task definition.

## Jenkins Requirements

Install/configure these on the Jenkins agent:

- Java 17
- Maven
- Docker
- AWS CLI v2
- Git

## Jenkins Credential

Create one Jenkins credential:

- Kind: `Username with password`
- ID: `aws-jenkins-user`
- Username: your AWS access key ID
- Password: your AWS secret access key

For production, prefer an IAM role attached to the Jenkins agent instead of long-lived access keys.

## AWS Resources Needed Once

Create these once before running the pipeline:

- ECR repository: `todo-api`
- ECS cluster: `todo-api-cluster`
- ECS service: `todo-api-service`
- ECS task execution role: `ecsTaskExecutionRole`
- Security group allowing inbound traffic to port `8080`, or an ALB forwarding to container port `8080`
- CloudWatch log group: `/ecs/todo-api`  
  The Jenkinsfile also tries to create this automatically if the IAM user has permission.

## Values to Change in Jenkinsfile

Update these values before committing/pushing:

```groovy
AWS_REGION = 'ap-south-1'
AWS_ACCOUNT_ID = '123456789012'
ECR_REPOSITORY = 'todo-api'
ECS_CLUSTER = 'todo-api-cluster'
ECS_SERVICE = 'todo-api-service'
```

## IAM Permissions Needed by Jenkins

For learning, you can attach broader permissions temporarily, but a cleaner Jenkins IAM policy should allow only the required ECR, ECS, IAM PassRole, and CloudWatch Logs actions.

Minimum style permissions include:

- `ecr:GetAuthorizationToken`
- `ecr:CreateRepository`
- `ecr:DescribeRepositories`
- `ecr:BatchCheckLayerAvailability`
- `ecr:InitiateLayerUpload`
- `ecr:UploadLayerPart`
- `ecr:CompleteLayerUpload`
- `ecr:PutImage`
- `ecs:RegisterTaskDefinition`
- `ecs:UpdateService`
- `ecs:DescribeServices`
- `iam:PassRole` for `ecsTaskExecutionRole`
- `logs:CreateLogGroup`

## Local Docker Test

From project root:

```bash
mvn clean package
docker build -t todo-api:local .
docker run -p 8080:8080 todo-api:local
```

Then test:

```bash
curl http://localhost:8080/api/todos
curl http://localhost:8080/actuator/health
```

## Important Note About H2

This learning API uses an H2 in-memory database. On ECS, data will reset when the container restarts. For real projects, use an external database such as Amazon RDS PostgreSQL or MySQL.
