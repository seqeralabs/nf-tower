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

package io.seqera.tower.domain

import java.nio.file.Files
import java.nio.file.Paths

import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
import org.nustaq.serialization.FSTConfiguration
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Transactional
@MicronautTest(application = Application.class)
class WorkflowLoadTest extends AbstractContainerBaseTest {


    def 'should save and load list of executors' () {
        given:
        def creator = new DomainCreator()
        def load = creator.createWorkflowLoad(executors: ['alpha','beta','delta'])

        when:
        def record = WorkflowLoad.get( load.id )
        then:
        record.executors ==  ['alpha','beta','delta']

    }

    def 'should add task stats' () {
        given:
        def workflow = new WorkflowLoad()

        when:
        workflow.incStats( new Task() )
        then:
        with(workflow) {
            cpus == 0
            cpuTime == 0
            cpuLoad == 0
            memoryRss == 0
            memoryReq == 0
            readBytes == 0
            writeBytes == 0
            volCtxSwitch == 0
            invCtxSwitch == 0 
        }

        when:
        workflow.incStats( new Task(
                cpus: 1,
                realtime: 200,
                pcpu: 200,
                peakRss: 300,
                memory: 400,
                rchar: 500,
                wchar: 600,
                volCtxt: 700,
                invCtxt: 800,
                cost: 1000 ))
        then:
        with(workflow) {
            cpus == 1
            cpuTime == 1 * 200
            cpuLoad == 200 / 100 * 200 as long
            memoryRss == 300
            memoryReq == 400
            readBytes == 500
            writeBytes == 600
            volCtxSwitch == 700
            invCtxSwitch == 800
            cost == 1000
        }


        when:
        workflow.incStats( new Task(
                cpus: 2,
                realtime: 300,
                pcpu: 400,
                peakRss: 1300,
                memory: 1400,
                rchar: 1500,
                wchar: 1600,
                volCtxt: 1700,
                invCtxt: 1800,
                cost: 11000 ))
        then:
        with(workflow) {
            cpus == 3
            cpuTime == 200 + ( 2 * 300 )
            cpuLoad == 400 + ( 400 / 100 * 300 as long)
            memoryRss == 1600
            memoryReq == 1800
            readBytes == 2000
            writeBytes == 2200
            volCtxSwitch == 2400
            invCtxSwitch == 2600
            cost == 12000
        }
    }


    static FSTConfiguration fstConf = FSTConfiguration.createDefaultConfiguration()

    def 'should ser-deser progress state' () {
        given:
        def state = new WorkflowLoad(
                running: 1,
                submitted: 2,
                pending: 3,
                executors: ['local','batch'] )

        when:
        byte[] buffer = fstConf.asByteArray(state)
        then:
        def copy = (WorkflowLoad)fstConf.asObject(buffer)
        and:
        state == copy

    }

    def 'should deserialize bin state' () {
        given:
        def state = new WorkflowLoad(
                running: 1,
                submitted: 2,
                pending: 3,
                executors: ['local','batch'] )
        and:
        def buffer = Files.readAllBytes(Paths.get('./src/test/resources/serialization/WorkflowLoad.fst.bin'))

        expect:
        fstConf.asObject(buffer) == state
    }
}
