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

package io.seqera.tower.service


import io.seqera.tower.domain.User
import io.seqera.tower.domain.Workflow
import io.seqera.tower.domain.WorkflowComment
import io.seqera.tower.domain.WorkflowMetrics
import io.seqera.tower.exchange.trace.TraceWorkflowRequest

interface WorkflowService {

    /**
     * Get a {@link Workflow} by its primary the key
     *
     * @param id The workflow primary key
     * @return The {@link Workflow} for the specified key of {@code null} if not match is found
     */
    Workflow get(Serializable id)

    /**
     * List all {@link Workflow} objects for the given owner {@link User}
     *
     * @param owner The owner {@link User} object
     * @param max The max number of elements to obtain
     * @param offset The offset number to obtain the elements from
     * @param sqlRegex A SQL regex to match some properties with (optional)
     * @return The list of workflow objects associated to the specified user
     */
    List<Workflow> listByOwner(User owner, Long max, Long offset, String sqlRegex)

    /**
     * Delete the specified {@link Workflow} object
     *
     * @param workflow
     */
    void delete(Workflow workflow)

    void deleteById(Serializable workflowId)

    Workflow processTraceWorkflowRequest(TraceWorkflowRequest request, User owner)

    List<WorkflowMetrics> findMetrics(Workflow workflow)

    List<WorkflowComment> getComments(Workflow workflow)


}
