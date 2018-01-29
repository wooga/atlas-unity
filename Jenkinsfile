#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@0.0.3') _

pipeline {
    agent none

    stages {
        stage('Preparation') {
            agent any

            steps {
                sendSlackNotification "STARTED", true
            }
        }

        stage('check') {
            parallel {
                stage('Windows') {
                    agent {
                        label 'unity&&windows&&unity_2017.1.0p5e&&atlas'
                    }

                    environment {
                        artifactoryCredentials  = credentials('artifactory_publish')
                        nugetkey                = credentials('artifactory_deploy')
                        COVERALLS_REPO_TOKEN    = credentials('atlas_unity_coveralls_token')
                        TRAVIS_JOB_NUMBER       = "${BUILD_NUMBER}.WIN"
                        UNITY_PATH              = "${UNITY_2017_1_0_P_5_PATH}"
                    }

                    steps {
                        gradleWrapper "check"
                    }

                    post {
                        success {
                            gradleWrapper "jacocoTestReport coveralls"
                        }

                        always {
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'build/reports/jacoco/test/html',
                                reportFiles: 'index.html',
                                reportName: 'Coverage',
                                reportTitles: ''
                            ])

                            junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
                            gradleWrapper "clean"
                        }
                    }
                }

                stage('macOS') {
                    agent {
                        label 'unity&&osx&&unity_2017.1.0p5e&&atlas'
                    }

                    environment {
                        artifactoryCredentials  = credentials('artifactory_publish')
                        nugetkey                = credentials('artifactory_deploy')
                        COVERALLS_REPO_TOKEN    = credentials('atlas_unity_coveralls_token')
                        TRAVIS_JOB_NUMBER       = "${BUILD_NUMBER}.WIN"
                        UNITY_PATH              = "${UNITY_2017_1_0_P_5_PATH}"
                    }

                    steps {
                        gradleWrapper "check"
                    }

                    post {
                        success {
                            gradleWrapper "jacocoTestReport coveralls"
                        }

                        always {
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'build/reports/jacoco/test/html',
                                reportFiles: 'index.html',
                                reportName: 'Coverage',
                                reportTitles: ''
                            ])

                            junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
                            gradleWrapper "clean"
                        }
                    }
                }
            }

            post {
                always {
                    sendSlackNotification currentBuild.result, true
                }
            }
        }
    }
}