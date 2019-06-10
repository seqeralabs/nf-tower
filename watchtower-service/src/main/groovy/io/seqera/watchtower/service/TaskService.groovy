package io.seqera.watchtower.service

import grails.gorm.PagedResultList
import io.seqera.watchtower.domain.Task
import io.seqera.watchtower.pogo.exchange.trace.TraceTaskRequest

interface TaskService {

    Task processTaskJsonTrace(TraceTaskRequest taskJson)

    PagedResultList<Task> findTasks(Long workflowId, Long max, Long offset)

}