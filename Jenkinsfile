#!groovy
@Library("Reform") _

properties([
        [$class: 'GithubProjectProperty', displayName: 'Payment API acceptance tests', projectUrlStr: 'https://git.reform.hmcts.net/common-components/payment-app-acceptance-tests'],
        parameters([
                string(defaultValue: 'latest', description: 'payments-api Docker Version', name: 'paymentsApiDockerVersion'),
                string(defaultValue: 'latest', description: 'payments-database Docker Version', name: 'paymentsDatabaseDockerVersion')
        ])
])


lock('Payment API acceptance tests') {
    node {
        try {
            stage('Checkout') {
                deleteDir()
                checkout scm
            }

            try {
                stage('Start Docker Images') {
                    env.PAYMENTS_API_DOCKER_VERSION = params.paymentsApiDockerVersion
                    env.PAYMENTS_DATABASE_DOCKER_VERSION = params.paymentsDatabaseDockerVersion

                    sh 'docker-compose pull'
                    sh 'docker-compose up -d payments-api'
                    sh 'docker-compose up wait-for-startup'
                }

                stage('Run acceptance tests') {
                    def rtMaven = Artifactory.newMavenBuild()
                    rtMaven.tool = 'apache-maven-3.3.9'
                    rtMaven.run pom: 'pom.xml', goals: 'clean package surefire-report:report -Dspring.profiles.active=docker -Dtest=**/acceptancetests/*Test'

                    publishHTML([
                            allowMissing         : false,
                            alwaysLinkToLastBuild: true,
                            keepAll              : false,
                            reportDir            : 'target/site',
                            reportFiles          : 'surefire-report.html',
                            reportName           : 'Acceptance Test Report'
                    ])
                }
            } finally {
                stage('Stop Docker Images') {
                    sh 'docker-compose down'
                }
            }
        } catch (err) {
            notifyBuildFailure channel: '#cc-payments-tech'
            throw err
        }
    }
}