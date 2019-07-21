package io.seqera.tower.exchange.workflow

import io.seqera.tower.domain.WorkflowMetrics
import io.seqera.tower.exchange.BaseResponse

/**
 * Response object to retrieve workflow metrics
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class GetWorkflowMetricsResponse implements BaseResponse {

    String message
    List<WorkflowMetrics> metrics

}
