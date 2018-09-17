#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@fix/lazy_env') _

withCredentials([usernameColonPassword(credentialsId: 'artifactory_publish', variable: 'artifactory_publish'),
                 usernameColonPassword(credentialsId: 'artifactory_deploy', variable: 'artifactory_deploy'),
                 string(credentialsId: 'atlas_unity_coveralls_token', variable: 'coveralls_token')]) {

    def testEnvironment = [
                            "artifactoryCredentials=${artifactory_publish}",
                            "nugetkey=${artifactory_deploy}",
                            {"UNITY_PATH=${APPLICATIONS_HOME}/Unity-2017.1.0p5/${UNITY_EXEC_PACKAGE_PATH}"}
                          ]

    buildGradlePlugin plaforms: ['osx','windows'],
                      coverallsToken: coveralls_token,
                      testEnvironment: testEnvironment,
                      labels: 'unity&&unity_2017.1.0p5e'
}
