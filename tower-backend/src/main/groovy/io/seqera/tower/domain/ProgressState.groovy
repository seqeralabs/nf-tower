package io.seqera.tower.domain

interface ProgressState {

    Long getRunning()
    Long getSubmitted()
    Long getFailed()
    Long getPending()
    Long getSucceeded()
    Long getCached()

    Long getCpus()
    Long getRealtime()
    Long getMemory()
    Long getDiskReads()
    Long getDiskWrites()

}