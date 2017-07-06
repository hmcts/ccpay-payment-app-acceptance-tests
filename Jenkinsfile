#!groovy
@Library("Reform")
import uk.gov.hmcts.Ansible
import uk.gov.hmcts.Packager
import uk.gov.hmcts.RPMTagger

def packager = new Packager(this, 'cc')
def ansible = new Ansible(this, 'ccpay')
def rtMaven = Artifactory.newMavenBuild()
RPMTagger rpmTagger = new RPMTagger(this, 'payment-api', packager.rpmName('payment-api', params.rpmVersion), 'cc-local')

properties([
        [$class: 'GithubProjectProperty', displayName: 'Payment API acceptance tests', projectUrlStr: 'https://git.reform.hmcts.net/common-components/payment-app-acceptance-tests'],
        parameters([string(defaultValue: '', description: 'RPM Version', name: 'rpmVersion')])
])

def secrets = [
        [$class      : 'VaultSecret',
         path        : 'secret/test/cc/payment/acceptance-tests/authorization-header',
         secretValues: [[$class: 'VaultSecretValue', envVar: 'SMOKE_TEST_HEADERS_AUTHORIZATION', vaultKey: 'value']]],
        [$class      : 'VaultSecret',
         path        : 'secret/test/cc/payment/acceptance-tests/service-authorization-header',
         secretValues: [[$class: 'VaultSecretValue', envVar: 'SMOKE_TEST_HEADERS_SERVICE_AUTHORIZATION', vaultKey: 'value']]]
]


lock('Payment API acceptance tests') {
    node {
        try {
            def deploymentRequired = !params.rpmVersion.isEmpty()
            def version = "{payment_api_version: ${params.rpmVersion}}"

            if (deploymentRequired) {
                stageWithNotification('Deploy to Dev') {
                    ansible.runDeployPlaybook(version, 'dev')
                    rpmTagger.tagDeploymentSuccessfulOn('dev')
                }
            }

            stage('Run acceptance tests') {
                deleteDir()
                checkout scm
                rtMaven.tool = 'apache-maven-3.3.9'
                rtMaven.run pom: 'pom.xml', goals: 'clean package surefire-report:report -Dspring.profiles.active=devA -Dtest=**/acceptancetests/*Test'

                publishHTML([
                        allowMissing         : false,
                        alwaysLinkToLastBuild: true,
                        keepAll              : false,
                        reportDir            : 'target/site',
                        reportFiles          : 'surefire-report.html',
                        reportName           : 'Acceptance Test Report'
                ])
            }

            if (deploymentRequired) {
                stage('Tag testing passed') {
                    rpmTagger.tagTestingPassedOn('dev')
                }

                stage('Deploy to Test') {
                    ansible.runDeployPlaybook(version, 'test')
                    rpmTagger.tagDeploymentSuccessfulOn('test')
                }

                stage('Run smoke tests') {
                    wrap([$class: 'VaultBuildWrapper', vaultSecrets: secrets]) {
                        deleteDir()
                        checkout scm
                        rtMaven.run pom: 'pom.xml', goals: 'clean package surefire-report:report -Dspring.profiles.active=devB -Dtest=**/smoketests/*Test'

                        publishHTML([
                                allowMissing         : false,
                                alwaysLinkToLastBuild: true,
                                keepAll              : false,
                                reportDir            : 'target/site',
                                reportFiles          : 'surefire-report.html',
                                reportName           : 'Smoke Test Report'
                        ])

                        rpmTagger.tagTestingPassedOn('test')
                    }
                }
            }
        } catch (err) {
            notifyBuildFailure channel: '#cc-payments-tech'
            throw err
        }
    }
}