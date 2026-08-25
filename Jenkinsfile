pipeline {
    agent any

    tools {
        jdk 'JDK-21'
        maven 'Maven-3.8.7'
    }

    environment {
        DOCKER_HUB_REPO = 'chakrigupta/git-team-todo-app'
        IMAGE_TAG = "${BUILD_NUMBER}"
        CONTAINER_NAME = 'git-team-todo-app'
        HOST_PORT = '8082'
        CONTAINER_PORT = '8082'
    }

    stages {

        // =========================================================
        // 1. CHECKOUT
        // =========================================================

        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'github-git-team-todo',
                    url: 'https://github.com/chakrigupta2001/git-team-todo-app.git'
            }
        }


        // =========================================================
        // 2. BUILD
        // =========================================================

        stage('Build') {
            steps {
                sh '''
                    set -e

                    echo "===== MAVEN BUILD ====="

                    mvn clean package
                '''
            }
        }


        // =========================================================
        // 3. TEST
        // =========================================================

        stage('Test') {
            steps {
                sh '''
                    set -e

                    echo "===== RUNNING TESTS ====="

                    mvn test
                '''
            }
        }


        // =========================================================
        // 4. SONARQUBE
        // =========================================================

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
                            set -e

                            echo "===== SONARQUBE ANALYSIS ====="

                            mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                              -Dsonar.projectKey=git-team-todo-app \
                              -Dsonar.projectName=git-team-todo-app \
                              -Dsonar.token=${SONAR_TOKEN}
                        '''
                    }
                }
            }
        }


        // =========================================================
        // 5. QUALITY GATE
        // =========================================================

        stage('Quality Gate') {
            steps {

                timeout(time: 5, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true
                }
            }
        }


        // =========================================================
        // 6. DOCKER BUILD + PUSH
        // =========================================================

        stage('Docker Build & Push') {
            steps {

                withCredentials([
                    usernamePassword(
                        credentialsId: 'docker-hub-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {

                    sh '''
                        set -e

                        echo "===== DOCKER LOGIN ====="

                        echo "$DOCKER_PASS" | docker login \
                            -u "$DOCKER_USER" \
                            --password-stdin


                        echo "===== DOCKER BUILD ====="

                        docker build \
                            -t ${DOCKER_HUB_REPO}:${IMAGE_TAG} \
                            -t ${DOCKER_HUB_REPO}:latest \
                            .


                        echo "===== DOCKER IMAGE CHECK ====="

                        docker images ${DOCKER_HUB_REPO}


                        echo "===== PUSH VERSIONED IMAGE ====="

                        docker push ${DOCKER_HUB_REPO}:${IMAGE_TAG}


                        echo "===== PUSH LATEST IMAGE ====="

                        docker push ${DOCKER_HUB_REPO}:latest
                    '''
                }
            }
        }


        // =========================================================
        // 7. DEPLOY
        // =========================================================

        stage('Deploy from Registry') {
            steps {

                sh '''
                    set -e

                    echo "===== STOP OLD CONTAINER ====="

                    docker stop ${CONTAINER_NAME} || true


                    echo "===== REMOVE OLD CONTAINER ====="

                    docker rm ${CONTAINER_NAME} || true


                    echo "===== PULL NEW IMAGE ====="

                    docker pull ${DOCKER_HUB_REPO}:${IMAGE_TAG}


                    echo "===== START NEW CONTAINER ====="

                    docker run -d \
                        --name ${CONTAINER_NAME} \
                        -p ${HOST_PORT}:${CONTAINER_PORT} \
                        ${DOCKER_HUB_REPO}:${IMAGE_TAG}


                    echo "===== CONTAINER STATUS ====="

                    docker ps
                '''
            }
        }


        // =========================================================
        // 8. APPLICATION HEALTH CHECK
        // =========================================================

        stage('Health Check') {
            steps {

                sh '''
                    echo "===== WAITING FOR APPLICATION ====="

                    sleep 10


                    echo "===== APPLICATION HEALTH CHECK ====="

                    if curl -f http://localhost:${HOST_PORT}; then

                        echo "======================================"
                        echo "APPLICATION IS UP"
                        echo "======================================"

                    else

                        echo "======================================"
                        echo "APPLICATION HEALTH CHECK FAILED"
                        echo "======================================"

                        echo "===== CONTAINER LOGS ====="

                        docker logs ${CONTAINER_NAME} || true

                        exit 1
                    fi
                '''
            }
        }
    }


    // =============================================================
    // POST ACTIONS
    // =============================================================

    post {

        success {

            echo '''
            ======================================
            PIPELINE SUCCESS
            ======================================
            '''
        }

        failure {

            echo '''
            ======================================
            PIPELINE FAILED
            ======================================
            '''

            sh '''
                echo "===== DOCKER CONTAINERS ====="

                docker ps -a || true


                echo "===== APPLICATION LOGS ====="

                docker logs ${CONTAINER_NAME} 2>/dev/null || true
            '''
        }

        always {

            sh '''
                echo "===== DOCKER LOGOUT ====="

                docker logout || true
            '''

            echo "Pipeline completed."
        }
    }
}