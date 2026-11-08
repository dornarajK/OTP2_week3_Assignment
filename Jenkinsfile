
pipeline {
    agent any

    options {
        timestamps()
    }

    environment {
        IMAGE_NAME = 'dornarajk/temperature-converter'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build, Test and Coverage') {
            steps {
                bat 'mvn -B clean verify'
            }

            post {
                always {
                    junit allowEmptyResults: true,
                            testResults: '**/target/surefire-reports/*.xml'

                    archiveArtifacts allowEmptyArchive: true,
                            artifacts: 'target/site/jacoco/**'
                }
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker build -t %IMAGE_NAME%:%IMAGE_TAG% -t %IMAGE_NAME%:latest .'
            }
        }

        stage('Docker Push') {
            when {
                branch 'main'
            }

            steps {
                withCredentials([
                        usernamePassword(
                                credentialsId: 'DockerHub',
                                usernameVariable: 'DOCKER_USERNAME',
                                passwordVariable: 'DOCKER_PASSWORD'
                        )
                ]) {
                    bat '''
                        echo %DOCKER_PASSWORD% | docker login --username %DOCKER_USERNAME% --password-stdin
                        docker push %IMAGE_NAME%:%IMAGE_TAG%
                        docker push %IMAGE_NAME%:latest
                    '''
                }
            }

            post {
                always {
                    bat 'docker logout'
                }
            }
        }
    }
}
