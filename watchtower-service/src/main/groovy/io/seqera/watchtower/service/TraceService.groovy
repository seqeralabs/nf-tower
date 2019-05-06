package io.seqera.watchtower.service

interface TraceService {

    Map<String, Object> createEntityByTrace(Map<String, Object> traceJson)

    Map<String, Object> processWorkflowTrace(Map<String, Object> traceJson)

    Map<String, Object> processTaskTrace(Map<String, Object> traceJson)

}

