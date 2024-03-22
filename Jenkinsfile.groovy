pipeline {
    agent any
    
    environment {
        PROJECT_ID = 'silencio-app'
        ARTIFACT_REGISTRY_LOCATION = 'europe-west3-docker.pkg.dev'
        REPOSITORY_NAME = 'silencio-admin-panel'
        IMAGE_NAME = 'silencio-admin-panel'
        IMAGE_TAG = 'staging'
        JSON_KEY_FILE = 'jk-file'
    }
    
    stages {
        stage("Checkout code") {
            steps {
                checkout scm
            }
        }
        
        stage('Build and Push Docker Image') {
            steps {
                script {
                    // Build Docker image
                    docker.build("${IMAGE_NAME}:${IMAGE_TAG}")

                    // Tag the image for Artifact Registry
                    def artifactTag = "${ARTIFACT_REGISTRY_LOCATION}/${REPOSITORY_NAME}/${IMAGE_NAME}:${IMAGE_TAG}"
                    docker.image("${IMAGE_NAME}:${IMAGE_TAG}").tag(artifactTag)

                    // Authenticate with JSON key file
                    withCredentials([file(credentialsId: 'json-key', variable: 'JSON_KEY_FILE')]) {
                        sh "gcloud auth activate-service-account --key-file=${JSON_KEY_FILE}"
                    }

                    // Push the image to Artifact Registry
                    sh "docker push ${artifactTag}"
                }
            }
        }
        
        stage('Deploy') {
            steps {
                // Your deployment steps go here
                // For example, if deploying to Cloud Run:
                // sh "gcloud run deploy ..."
            }
        }
    }    
}
