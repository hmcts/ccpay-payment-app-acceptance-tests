#!groovy
@Library("Reform")
import uk.gov.hmcts.Ansible
import uk.gov.hmcts.Packager
import uk.gov.hmcts.RPMTagger

def packager = new Packager(this, 'cc')
def ansible = new Ansible(this, 'ccpay')

def rtMaven = Artifactory.newMavenBuild()

properties([
        [$class: 'GithubProjectProperty', displayName: 'Payment API acceptance tests', projectUrlStr: 'https://git.reform.hmcts.net/common-components/payment-app-acceptance-tests'],
        parameters([string(defaultValue: '', description: 'RPM Version', name: 'rpmVersion')])
])

lock('Single Instance Only') {
    stageWithNotification('Delete old stuff') {
        deleteDir()
    }

    stageWithNotification('Compile tests') {
        checkout scm
        rtMaven.tool = 'apache-maven-3.3.9'
        rtMaven.run pom: 'pom.xml', goals: 'test-compile'
    }

    RPMTagger rpmTagger = new RPMTagger(this, 'payment-api', packager.rpmName('payment-api', params.rpmVersion), 'cc-local')
    def version = "{payment_api_version: ${params.rpmVersion}}"

    stageWithNotification('Deploy to Dev') {
        ansible.runDeployPlaybook(version, 'dev')
        rpmTagger.tagDeploymentSuccessfulOn('dev')
    }

    stageWithNotification('Run acceptance tests') {
        checkout scm
        rtMaven.run pom: 'pom.xml', goals: 'package'
        rpmTagger.tagTestingPassedOn('dev')
    }

    stageWithNotification('Deploy to Test') {
        ansible.runDeployPlaybook(version, 'test')
        rpmTagger.tagDeploymentSuccessfulOn('test')
    }

    stageWithNotification('Run smoke tests') {
        println 'Running smoke tests'
        rpmTagger.tagTestingPassedOn('test')
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
