
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn clean verify'
            }
        }

        stage('Code Coverage') {
            steps {
                bat 'mvn jacoco:report'
            }
        }

        stage('Publish Test Results') {
            steps {
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('Publish Coverage Report') {
            steps {
                jacoco()
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker build -t dornarajk/temperature-converter:latest .'
            }
        }

        stage('Docker Login and Push') {
            steps {
                withCredentials([
                        usernamePassword(
                                credentialsId: 'DockerHub',
                                usernameVariable: 'DOCKER_USERNAME',
                                passwordVariable: 'DOCKER_PASSWORD'
                        )
                ]) {
                    bat 'echo %DOCKER_PASSWORD%| docker login -u %DOCKER_USERNAME% --password-stdin'
                    bat 'docker push dornarajk/temperature-converter:latest'
                }
            }
        }
    }
}
