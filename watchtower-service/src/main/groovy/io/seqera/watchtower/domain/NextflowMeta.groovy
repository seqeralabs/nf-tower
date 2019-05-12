package io.seqera.watchtower.domain

import java.time.Instant

/**
 * Model Workflow nextflow attribute holding Nextflow metadata
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class NextflowMeta {
    String version
    String build
    Instant timestamp
}
