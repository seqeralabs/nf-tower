package io.seqera.tower.domain

interface ProgressState {

    Long getRunning()
    Long getSubmitted()
    Long getFailed()
    Long getPending()
    Long getSucceeded()
    Long getCached()

    Long getTotalCpus()
    Long getCpuRealtime()
    Long getMemory()
    Long getDiskReads()
    Long getDiskWrites()

}