#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@0.0.3') _

pipeline {
    agent {
        label 'unity&&windows&&unity_2017.1.0p5e'
    }

    environment {
        artifactoryCredentials  = credentials('artifactory_publish')
        nugetkey                = credentials('artifactory_deploy')
        COVERALLS_REPO_TOKEN    = credentials('atlas_unity_coveralls_token')
        TRAVIS_JOB_NUMBER       = "${BUILD_NUMBER}.WIN"
        UNITY_PATH              = "${UNITY_2017_1_0_P_5_PATH}"
    }

    stages {
        stage('Preparation') {
            steps {
                sendSlackNotification "STARTED", true
            }
        }

        stage('Test') {
            steps {
                gradleWrapper "check"
            }
        }
    }

    post {
        always {
            gradleWrapper "jacocoTestReport coveralls"
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'build/reports/jacoco/test/html',
                reportFiles: 'index.html',
                reportName: 'Coverage',
                reportTitles: ''
            ])

            sendSlackNotification currentBuild.result, true
            junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
            gradleWrapper "clean"
        }
    }
}