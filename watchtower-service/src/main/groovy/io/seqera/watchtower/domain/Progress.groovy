package io.seqera.watchtower.domain

/**
 * Model workflow progress counters
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class Progress {

    int running
    int submitted
    int failed
    int pending
    int succeeded
    int cached

}
