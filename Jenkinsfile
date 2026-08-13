pipeline {
    agent any

    tools {
        jdk 'JDK-21'
        maven 'Maven-3.8.7'
    }

    environment {
        DOCKER_HUB_REPO = 'chakrigupta/git-team-todo-app'
        IMAGE_TAG = '1.0.0'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'github-git-team-todo',
                    url: 'https://github.com/chakrigupta2001/git-team-todo-app.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    withCredentials([
                        string(
                            credentialsId: 'sq-token',
                            variable: 'SONAR_TOKEN'
                        )
                    ]) {
                        sh '''
                            mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                              -Dsonar.projectKey=git-team-todo-app \
                              -Dsonar.projectName=git-team-todo-app \
                              -Dsonar.token=${SONAR_TOKEN}
                        '''
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker build -t ${DOCKER_HUB_REPO}:${IMAGE_TAG} -t ${DOCKER_HUB_REPO}:latest .
                        docker push ${DOCKER_HUB_REPO}:${IMAGE_TAG}
                        docker push ${DOCKER_HUB_REPO}:latest
                    '''
                }
            }
        }

        stage('Deploy from Registry') {
            steps {
                sh '''
                    # Stop and remove existing container
                    docker stop git-team-todo-app || true
                    docker rm git-team-todo-app || true

                    # Remove local image to prove pulling from Docker Hub works
                    docker rmi ${DOCKER_HUB_REPO}:${IMAGE_TAG} || true

                    # Pull latest image from Docker Hub and run
                    docker run -d \
                      --name git-team-todo-app \
                      -p 8082:8082 \
                      ${DOCKER_HUB_REPO}:${IMAGE_TAG}
                '''
            }
        }
    }

    post {
        always {
            sh 'docker logout'
        }
    }
}