pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1'
        AWS_ACCOUNT_ID = '435556621081'

        ECR_REPOSITORY = 'todo-api'
        ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

        ECS_CLUSTER = 'todo-api-cluster'
        ECS_SERVICE = 'todo-api-task-service-tky48ft4'
        TASK_FAMILY = 'todo-api-task'
        CONTAINER_NAME = 'todo-api'

        TASK_EXECUTION_ROLE_ARN = "arn:aws:iam::${AWS_ACCOUNT_ID}:role/ecsTaskExecutionRole"
        LOG_GROUP = '/ecs/todo-api'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code from GitHub...'
                checkout scm
            }
        }

        stage('Build Spring Boot App') {
            steps {
                echo 'Building Java 17 Spring Boot application...'
                sh 'mvn clean package -DskipTests'
                sh 'ls -lh target/*.jar'
            }
        }

        stage('Prepare Image Tag') {
            steps {
                script {
                    def gitCommit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.IMAGE_TAG = "${BUILD_NUMBER}-${gitCommit}"
                    env.IMAGE_URI = "${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}"

                    echo "Image tag: ${IMAGE_TAG}"
                    echo "Image URI: ${IMAGE_URI}"
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'
                sh 'docker build -t ${ECR_REPOSITORY}:${IMAGE_TAG} .'
                sh 'docker tag ${ECR_REPOSITORY}:${IMAGE_TAG} ${IMAGE_URI}'
            }
        }

        stage('Push Image to ECR') {
            steps {
                echo 'Logging in to ECR...'
                sh '''
                    aws ecr get-login-password --region ${AWS_REGION} \
                    | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                '''

                echo 'Pushing Docker image to ECR...'
                sh 'docker push ${IMAGE_URI}'
            }
        }

        stage('Create ECS Task Definition JSON') {
            steps {
                echo 'Creating ECS task definition file...'

                sh '''
cat > task-definition.json <<EOF
{
  "family": "${TASK_FAMILY}",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "executionRoleArn": "${TASK_EXECUTION_ROLE_ARN}",
  "containerDefinitions": [
    {
      "name": "${CONTAINER_NAME}",
      "image": "${IMAGE_URI}",
      "essential": true,
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "${LOG_GROUP}",
          "awslogs-region": "${AWS_REGION}",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
EOF

cat task-definition.json
'''
            }
        }

        stage('Register Task Definition') {
            steps {
                echo 'Registering new ECS task definition revision...'

                script {
                    env.NEW_TASK_DEF_ARN = sh(
                        script: '''
                            aws ecs register-task-definition \
                              --cli-input-json file://task-definition.json \
                              --region ${AWS_REGION} \
                              --query 'taskDefinition.taskDefinitionArn' \
                              --output text
                        ''',
                        returnStdout: true
                    ).trim()

                    echo "New task definition ARN: ${NEW_TASK_DEF_ARN}"
                }
            }
        }

        stage('Update ECS Service') {
            steps {
                echo 'Updating ECS service with new task definition...'

                sh '''
                    aws ecs update-service \
                      --cluster ${ECS_CLUSTER} \
                      --service ${ECS_SERVICE} \
                      --task-definition ${NEW_TASK_DEF_ARN} \
                      --region ${AWS_REGION}
                '''
            }
        }

        stage('Wait for ECS Stability') {
            steps {
                echo 'Waiting until ECS service becomes stable...'

                sh '''
                    aws ecs wait services-stable \
                      --cluster ${ECS_CLUSTER} \
                      --services ${ECS_SERVICE} \
                      --region ${AWS_REGION}
                '''
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully. New version deployed to ECS.'
        }

        failure {
            echo 'Pipeline failed. Check the failed stage logs above.'
        }
    }
}
