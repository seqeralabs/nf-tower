/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.service.audit

import java.time.Instant
import java.time.OffsetDateTime

import io.seqera.tower.domain.User
import io.seqera.tower.domain.WfNextflow
import io.seqera.tower.domain.WfStats
import io.seqera.tower.domain.Workflow
import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class AuditEventPublisherTest extends Specification {

    def 'should build completion email' () {
        given:
        def service = new AuditEventPublisher()
        and:
        def user = new User(email: 'foo@goole.com')
        def workflow = new Workflow(owner: user)
        and:
        workflow.runName = 'foo_bar'
        workflow.success = true
        workflow.stats = new WfStats(
                computeTimeFmt:'xyz',
                succeedCountFmt: '10',
                cachedCountFmt: '20',
                ignoredCountFmt: '30',
                failedCountFmt: '40')
        workflow.commandLine = 'nextflow run -this -and-that'
        workflow.start = OffsetDateTime.now()
        workflow.complete = OffsetDateTime.now()
        workflow.duration = 1234
        workflow.launchDir = '/this/that/path'
        workflow.workDir = '/work/dir/path'
        workflow.projectDir = 'the/project/dir'
        workflow.scriptName = 'foo.nf'
        workflow.scriptId = '123456'
        workflow.sessionId = 'abc'
        workflow.repository = 'git://user/name'
        workflow.revision = 'aabbcc'
        workflow.commitId = 'xyz'
        workflow.profile = 'cloud'
        workflow.container = 'ubuntu:latest'
        workflow.containerEngine = 'docker'
        workflow.nextflow = new WfNextflow(version_: '19.11.1', build: '128', timestamp: Instant.now())

        when:
        def mail = service.buildCompletionEmail(workflow)
        then:
        mail.getTo() == 'foo@goole.com'
        mail.getSubject() == 'Workflow completion [foo_bar] - SUCCEED!'
        mail.getBody().contains('Execution completed successfully!')

        when:
        workflow.success = false
        and:
        mail = service.buildCompletionEmail(workflow)
        then:
        mail.getTo() == 'foo@goole.com'
        mail.getSubject() == 'Workflow completion [foo_bar] - FAILED!'
        mail.getBody().contains('Execution completed unsuccessfully!')
    }
}
