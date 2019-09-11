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

package io.seqera.tower.exchange.trace.sse

import io.seqera.tower.enums.WorkflowAction
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class TraceSseResponseTest extends Specification {

    def 'should create event' () {
        when:
        def resp = TraceSseResponse.of(10, 'xyz', WorkflowAction.WORKFLOW_UPDATE)
        then:
        resp.userId == 10
        resp.workflowId == 'xyz'
        resp.action == WorkflowAction.WORKFLOW_UPDATE
        resp.message==null

        when:
        def resp2 = TraceSseResponse.of(20, 'abc', WorkflowAction.PROGRESS_UPDATE)
        then:
        resp2.userId == 20
        resp2.workflowId == 'abc'
        resp2.action == WorkflowAction.PROGRESS_UPDATE
        resp2.message==null

        when:
        def resp3 = TraceSseResponse.ofError('Foo')
        then:
        resp3.userId == null
        resp3.workflowId == null
        resp3.action == null
        resp3.message=='Foo'
    }

    def 'should implements equality' () {
        when:
        def resp1 = new TraceSseResponse(100, '200', null)
        def resp2 = new TraceSseResponse(100, '200', null)
        def resp3 = new TraceSseResponse(100, '300', null)

        then:
        resp1 == resp2
        resp1 != resp3
    }

    def 'should render as a string' () {
        when:
        def resp = TraceSseResponse.of(100, '200')
        then:
        resp.toString() == 'TraceSseResponse[userId=100; workflowId=200; action=null]'

        when:
        resp = TraceSseResponse.of(100, '200', WorkflowAction.WORKFLOW_UPDATE)
        then:
        resp.toString() == 'TraceSseResponse[userId=100; workflowId=200; action=WORKFLOW_UPDATE]'

        when:
        resp = TraceSseResponse.ofError('Hello')
        then:
        resp.toString() == "TraceSseResponse[userId=null; workflowId=null; action=null; message='Hello']"

    }
}
