pipeline {
    agent any
    
    environment {
        JQL = 'project = "Testing Jira"'
        JIRA_TOKEN = credentials('') 
    }
    
    stages {
        stage('Checkout') {
            steps {
                git url: 'https://gitlab.com/wholesale-platform-cxo/devsecops/shared-library.git', branch: 'master'
            }
        }
        
        stage('Build') {
            steps {
                script {
                    try {
                        sh 'mvn clean install' 
                        currentBuild.result = 'SUCCESS'
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        throw e
                    }
                }
            }
        }
        
        stage('Update Jira') {
            steps {
                script {
                
                    def previousStatus = sh(
                        script: "curl -s -X GET -H 'Authorization: Bearer ${JIRA_TOKEN}' \
                        https://jira.digi.id/rest/api/2/issue/${JQL} | jq -r '.fields.status.name'",
                        returnStdout: true
                    ).trim()
                    
                    def currentStatus = currentBuild.result == 'SUCCESS' ? 'In PROGRESS' : 'BACKLOG'

                    if (previousStatus == 'BACKLOG' && currentStatus == 'In PROGRESS') {
            
                        sh """
                        curl -X POST -H "Authorization: Bearer ${JIRA_TOKEN}" -H "Content-Type: application/json" \
                        --data '{"body": "Ticket moved from Backlog to In Progress automatically."}' \
                        https://jira.digi.id/rest/api/2/issue/${JQL}/comment
                        """
                    }
                }
            }
        }
    }
    
    post {
        success {
            echo 'Build was successful!'
        }
        failure {
            echo 'Build failed.'
        }
    }
}
