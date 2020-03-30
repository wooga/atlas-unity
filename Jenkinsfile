#!groovy

@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([usernameColonPassword(credentialsId: 'artifactory_publish', variable: 'artifactory_publish'),
                 usernameColonPassword(credentialsId: 'artifactory_deploy', variable: 'artifactory_deploy'),
                 string(credentialsId: 'atlas_unity_coveralls_token', variable: 'coveralls_token')]) {

    def testEnvironment = [
                            "artifactoryCredentials=${artifactory_publish}",
                            "nugetkey=${artifactory_deploy}"
                          ]

    buildGradlePlugin plaforms: ['osx','windows','linux'],
                      coverallsToken: coveralls_token,
                      testEnvironment: testEnvironment,
                      labels: 'primary'
}
