def getSsmParam(String name) {
    return sh(
        script: "aws ssm get-parameter --name '${name}' --query 'Parameter.Value' --output text --region ${env.BOOTSTRAP_REGION}",
        returnStdout: true
    ).trim()
}

pipeline {
    agent any

    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['dev', 'test', 'prod'],
            description: 'Target deployment environment'
        )
    }

    environment {
        // Bootstrap region is needed to read SSM parameters.
        // This is not secret. You can also set it in Jenkins Global Environment instead.
        BOOTSTRAP_REGION = 'us-east-1'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Load Deployment Config from SSM') {
            steps {
                script {
                    def basePath = "/todo-api/${params.DEPLOY_ENV}"

                    env.AWS_REGION = getSsmParam("${basePath}/aws-region")
                    env.AWS_ACCOUNT_ID = sh(
                        script: "aws sts get-caller-identity --query Account --output text --region ${env.AWS_REGION}",
                        returnStdout: true
                    ).trim()

                    env.ECR_REPOSITORY = getSsmParam("${basePath}/ecr-repository")
                    env.ECS_CLUSTER = getSsmParam("${basePath}/ecs-cluster")
                    env.ECS_SERVICE = getSsmParam("${basePath}/ecs-service")
                    env.TASK_FAMILY = getSsmParam("${basePath}/task-family")
                    env.CONTAINER_NAME = getSsmParam("${basePath}/container-name")
                    env.LOG_GROUP = getSsmParam("${basePath}/log-group")
                    env.TASK_EXECUTION_ROLE_ARN = getSsmParam("${basePath}/task-execution-role-arn")

                    env.ECR_REGISTRY = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com"

                    echo "Deployment environment: ${params.DEPLOY_ENV}"
                    echo "AWS region: ${env.AWS_REGION}"
                    echo "ECR repository: ${env.ECR_REPOSITORY}"
                    echo "ECS cluster: ${env.ECS_CLUSTER}"
                    echo "ECS service: ${env.ECS_SERVICE}"
                }
            }
        }

        stage('Build Spring Boot App') {
            steps {
                sh 'mvn clean package -DskipTests'
                sh 'ls -lh target/*.jar'
            }
        }

        stage('Prepare Image Tag') {
            steps {
                script {
                    def gitCommit = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    env.IMAGE_TAG = "${BUILD_NUMBER}-${gitCommit}"
                    env.IMAGE_URI = "${env.ECR_REGISTRY}/${env.ECR_REPOSITORY}:${env.IMAGE_TAG}"

                    echo "Image URI: ${env.IMAGE_URI}"
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t ${ECR_REPOSITORY}:${IMAGE_TAG} .'
                sh 'docker tag ${ECR_REPOSITORY}:${IMAGE_TAG} ${IMAGE_URI}'
            }
        }

        stage('Push Image to ECR') {
            steps {
                sh '''
                    aws ecr get-login-password --region ${AWS_REGION} \
                    | docker login --username AWS --password-stdin ${ECR_REGISTRY}

                    docker push ${IMAGE_URI}
                '''
            }
        }

        stage('Create ECS Task Definition JSON') {
            steps {
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

                    echo "New task definition ARN: ${env.NEW_TASK_DEF_ARN}"
                }
            }
        }

        stage('Update ECS Service') {
            steps {
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
            echo "Deployment completed successfully."
        }
        failure {
            echo "Deployment failed. Check the failed stage logs."
        }
    }
}
