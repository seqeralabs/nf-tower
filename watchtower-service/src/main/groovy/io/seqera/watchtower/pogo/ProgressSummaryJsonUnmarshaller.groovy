package io.seqera.watchtower.pogo

import groovy.transform.CompileDynamic
import io.seqera.watchtower.domain.ProgressSummary

class ProgressSummaryJsonUnmarshaller {


    @CompileDynamic
    static void populateProgressSummaryFields(Map<String, Object> progressSummaryJson, ProgressSummary progressSummary) {
        progressSummaryJson.each { String k, def v ->
            if (!isIgnoredField(k, progressSummary)) {
                progressSummary[k] = v
            }
        }
    }

    private static boolean isIgnoredField(String key, ProgressSummary progressSummary) {
        !progressSummary.hasProperty(key)
    }

}
