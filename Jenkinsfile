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
    def deploymentRequired = !params.rpmVersion.isEmpty()
    def version = "{payment_api_version: ${params.rpmVersion}}"

    stageWithNotification('Delete old stuff') {
        deleteDir()
    }

    stageWithNotification('Compile tests') {
        checkout scm
        rtMaven.tool = 'apache-maven-3.3.9'
        rtMaven.run pom: 'pom.xml', goals: 'test-compile'
    }

    if (deploymentRequired) {
        stageWithNotification('Deploy to Dev') {
            ansible.runDeployPlaybook(version, 'dev')
            rpmTagger.tagDeploymentSuccessfulOn('dev')
        }
    }

    stageWithNotification('Run acceptance tests') {
        checkout scm
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
        stageWithNotification('Tag testing passed') {
            rpmTagger.tagTestingPassedOn('dev')
        }

        stageWithNotification('Deploy to Test') {
            ansible.runDeployPlaybook(version, 'test')
            rpmTagger.tagDeploymentSuccessfulOn('test')
        }

        stageWithNotification('Run smoke tests') {
            wrap([$class: 'VaultBuildWrapper', vaultSecrets: secrets]) {
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
}

private stageWithNotification(String name, Closure body) {
    stage(name) {
        node {
            try {
                body()
            } catch (err) {
                notifyBuildFailure channel: '#cc_tech'
                throw err
            }
        }
    }
}
