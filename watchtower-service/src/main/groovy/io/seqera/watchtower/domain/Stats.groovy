package io.seqera.watchtower.domain

/**
 * Model workflow stats
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class Stats {

    String computeTimeFmt

    int cachedCount
    int failedCount
    int ignoredCount
    int succeedCount

    String cachedCountFmt
    String succeedCountFmt
    String failedCountFmt
    String ignoredCountFmt

    float cachedPct
    float failedPct
    float succeedPct
    float ignoredPct

    long cachedDuration
    long failedDuration
    long succeedDuration
}
