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

package io.seqera.tower.service.progress

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.seqera.tower.domain.ProcessLoad
import io.seqera.tower.domain.WorkflowLoad

/**
 * Model a workflow execution progress metadata
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
@EqualsAndHashCode
@ToString(includeNames = true, includePackage = false)
class ProgressState implements Serializable {

    String workflowId
    private List<String> processNames
    private Map<String, ProcessLoad> processes
    private WorkflowLoad workflow

    ProgressState(String workflowId, List<String> processNames) {
        assert processNames != null
        this.workflowId = workflowId
        this.processNames = processNames
        this.processes = new HashMap<>(processNames.size())
        for( String name : processNames )
            processes[name] = new ProcessLoad(process:name)
        this.workflow = new WorkflowLoad()
    }

    List<ProcessLoad> getProcesses() {
        def result = new ArrayList(processNames.size())
        for( String name : processNames ) {
            if( !name ) {
                log.warn "Unexpected empty process name for workflow Id=$workflowId"
                continue
            }
            def el = processes.get(name)
            if( el == null ) {
                log.warn "Missing progress stats for process $processes; workflow Id=$workflowId"
                el = new ProcessLoad(process:name)
            }
            result.add(el)
        }
        return result
    }

    WorkflowLoad getWorkflow() { workflow }

    ProcessLoad getState(String processName) {
        processes.computeIfAbsent(processName, {new ProcessLoad()})
    }

    void updatePeaks() {
        workflow = new WorkflowLoad(
                peakTasks: workflow.peakTasks,
                peakCpus: workflow.peakCpus,
                peakMemory: workflow.peakMemory )

        for( ProcessLoad state : processes.values() ) {
            state.updatePeaks()
            workflow.incStatsAndLoad(state)
        }

        workflow.updatePeaks()
    }

}
