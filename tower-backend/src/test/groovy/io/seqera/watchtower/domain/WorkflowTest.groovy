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

package io.seqera.watchtower.domain

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

import com.fasterxml.jackson.databind.ObjectMapper
import io.seqera.watchtower.exchange.trace.TraceWorkflowRequest
import spock.lang.Specification

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class WorkflowTest extends Specification {

    private final static RESOURCES_DIR_PATH = 'src/test/resources'

    def 'should de-serialize a workflow object' () {
        given:
        def json = new File('src/test/resources/workflow_success/1_workflow_started.json')
        def mapper = new ObjectMapper().findAndRegisterModules()

        when:
        def request = mapper .readValue(json, TraceWorkflowRequest)
        then:
        request.workflow.with {
            projectDir == "/home/user/.nextflow/assets/nextflow-io/hello"
            start == OffsetDateTime.parse("2019-07-05T00:43:45.945+02:00")
            profile == 'standard'
            container == 'nextflow/bash'
            commitId == "a9012339ce857d6ec7a078281813d8a93645a3e7"
            configFiles == "/home/user/.nextflow/assets/nextflow-io/hello/nextflow.config"
            manifest.defaultBranch == 'master'
            manifest.mainScript == 'main.nf'
            nextflow.version == "19.05.0-TOWER"
            nextflow.build == 5078
            nextflow.timestamp == Instant.parse("2019-05-05T16:30:00Z")
        }
    }

    def 'should convert time' () {

        when:
        def x = Instant.ofEpochMilli(1561353544855)
        println OffsetDateTime.ofInstant(x, ZoneId.systemDefault()).toString()
        then:
        true

    }
}
