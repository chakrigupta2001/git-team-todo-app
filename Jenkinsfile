pipeline {
    agent any

    tools {
        jdk 'JDK-21'
        maven 'Maven-3.8.7'
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

        stage('Docker Build') {
            steps {
                sh 'docker build -t git-team-todo-app:1.0.0 .'
            }
        }

        stage('Docker Deploy') {
            steps {
                sh '''
                    docker stop git-team-todo-app || true
                    docker rm git-team-todo-app || true

                    docker run -d \
                      --name git-team-todo-app \
                      -p 8082:8082 \
                      git-team-todo-app:1.0.0
                '''
            }
        }
    }
}