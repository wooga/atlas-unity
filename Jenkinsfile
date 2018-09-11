#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([usernameColonPassword(credentialsId: 'artifactory_publish', variable: 'artifactory_publish'),
                 usernameColonPassword(credentialsId: 'artifactory_deploy', variable: 'artifactory_deploy'),
                 string(credentialsId: 'atlas_unity_coveralls_token', variable: 'coveralls_token')]) {

    def testEnvironment = [
                            "artifactoryCredentials=${artifactory_publish}",
                            "nugetkey=${artifactory_deploy}",
                            "UNITY_PATH=${env.UNITY_2017_1_0_P_5_PATH}"
                          ]

    buildGradlePlugin plaforms: ['unity&&osx&&unity_2017.1.0p5e','unity&&windows&&unity_2017.1.0p5e'], coverallsToken: coveralls_token, testEnvironment: testEnvironment
}
