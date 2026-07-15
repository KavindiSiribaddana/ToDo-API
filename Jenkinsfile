/*
 Jenkins CI/CD pipeline for the Spring Boot Todo API.

 What this pipeline does:
 1. Checks out source code from GitHub.
 2. Builds and tests the Java 17 Spring Boot project using Maven.
 3. Builds a Docker image.
 4. Logs in to Amazon ECR.
 5. Pushes the Docker image to ECR.
 6. Registers a new ECS task definition revision.
 7. Updates an existing ECS service so ECS deploys the new task.

 Jenkins agent requirements:
 - Java 17
 - Maven
 - Docker CLI/daemon access
 - AWS CLI v2
 - Jenkins credential named aws-jenkins-user
   Type: Username with password
   Username: AWS_ACCESS_KEY_ID
   Password: AWS_SECRET_ACCESS_KEY
*/

pipeline {
    agent any

    environment {
        // Replace these values with your AWS/account details.
        AWS_REGION = 'ap-south-1'
        AWS_ACCOUNT_ID = '123456789012'

        // ECR repository name. Example final image:
        // 123456789012.dkr.ecr.ap-south-1.amazonaws.com/todo-api:15-a1b2c3d
        ECR_REPOSITORY = 'todo-api'

        // Existing ECS cluster/service names.
        // Create these once in AWS Console or AWS CLI before running the deployment stage.
        ECS_CLUSTER = 'todo-api-cluster'
        ECS_SERVICE = 'todo-api-service'

        // Must match the container name inside aws/task-definition-template.json.
        CONTAINER_NAME = 'todo-api'

        // CloudWatch log group used by the ECS task definition.
        CLOUDWATCH_LOG_GROUP = '/ecs/todo-api'
    }

    stages {
        stage('Checkout') {
            steps {
                // For a Multibranch Pipeline, Jenkins automatically checks out the branch.
                // For a normal Pipeline job, configure the GitHub repo in Jenkins job settings.
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                // clean  : removes old build output
                // verify : compiles, runs tests, and packages the Spring Boot JAR
                sh 'mvn -B clean verify'
            }
        }

        stage('Prepare Image Tag') {
            steps {
                script {
                    // Short commit hash helps trace which Git commit produced each Docker image.
                    def shortCommit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${shortCommit}"
                    env.ECR_REGISTRY = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com"
                    env.IMAGE_URI = "${env.ECR_REGISTRY}/${env.ECR_REPOSITORY}:${env.IMAGE_TAG}"
                    echo "Docker image will be: ${env.IMAGE_URI}"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                // Dockerfile copies the JAR from target/*.jar into the runtime image.
                sh 'docker build -t ${IMAGE_URI} .'
            }
        }

        stage('Login and Push to ECR') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'aws-jenkins-user',
                    usernameVariable: 'AWS_ACCESS_KEY_ID',
                    passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                )]) {
                    sh '''
                        set -e

                        # Make AWS CLI use the configured region.
                        export AWS_DEFAULT_REGION=${AWS_REGION}

                        # Create ECR repository if it does not already exist.
                        aws ecr describe-repositories --repository-names ${ECR_REPOSITORY} >/dev/null 2>&1 \
                          || aws ecr create-repository --repository-name ${ECR_REPOSITORY} >/dev/null

                        # Authenticate Docker to the private ECR registry.
                        aws ecr get-login-password --region ${AWS_REGION} \
                          | docker login --username AWS --password-stdin ${ECR_REGISTRY}

                        # Push the build-specific image tag.
                        docker push ${IMAGE_URI}
                    '''
                }
            }
        }

        stage('Deploy to ECS') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'aws-jenkins-user',
                    usernameVariable: 'AWS_ACCESS_KEY_ID',
                    passwordVariable: 'AWS_SECRET_ACCESS_KEY'
                )]) {
                    sh '''
                        set -e
                        export AWS_DEFAULT_REGION=${AWS_REGION}

                        # Create CloudWatch log group if it does not already exist.
                        aws logs create-log-group --log-group-name ${CLOUDWATCH_LOG_GROUP} >/dev/null 2>&1 || true

                        # Replace placeholders in the ECS task definition template.
                        sed \
                          -e "s|<AWS_ACCOUNT_ID>|${AWS_ACCOUNT_ID}|g" \
                          -e "s|<AWS_REGION>|${AWS_REGION}|g" \
                          -e "s|<IMAGE_URI>|${IMAGE_URI}|g" \
                          aws/task-definition-template.json > task-definition.json

                        # Register a new ECS task definition revision using the new Docker image.
                        TASK_DEF_ARN=$(aws ecs register-task-definition \
                          --cli-input-json file://task-definition.json \
                          --query 'taskDefinition.taskDefinitionArn' \
                          --output text)

                        echo "Registered task definition: ${TASK_DEF_ARN}"

                        # Update the ECS service. ECS then starts new tasks using the new revision.
                        aws ecs update-service \
                          --cluster ${ECS_CLUSTER} \
                          --service ${ECS_SERVICE} \
                          --task-definition ${TASK_DEF_ARN} \
                          --force-new-deployment >/dev/null

                        # Wait until the ECS service becomes stable.
                        aws ecs wait services-stable \
                          --cluster ${ECS_CLUSTER} \
                          --services ${ECS_SERVICE}

                        echo "Deployment completed successfully."
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "CI/CD completed. Image pushed and ECS service updated: ${IMAGE_URI}"
        }
        failure {
            echo 'Pipeline failed. Check the failed stage logs in Jenkins.'
        }
    }
}
