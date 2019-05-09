package io.seqera.watchtower.pogo.unmarshaller

import groovy.transform.CompileDynamic
import io.seqera.watchtower.domain.MagnitudeSummary

class MagnitudeSummaryJsonUnmarshaller {


    static Collection<MagnitudeSummary> extractAllMagnitudeSummaries(Map<String, Object> magnitudeSummaryTasksJson) {
        (Collection<MagnitudeSummary>) magnitudeSummaryTasksJson.findResults { Map.Entry<String, Object> entry ->
            extractMagnitudeSummariesOfTask((Map<String, Object>) entry.value, entry.key)
        }.flatten()
    }

    static Collection<MagnitudeSummary> extractMagnitudeSummariesOfTask(Map<String, Object> magnitudeSummaryMagnitudesJson, String taskLabel) {
        magnitudeSummaryMagnitudesJson.findResults { Map.Entry<String, Object> entry ->
            entry.value ? buildMagnitudeSummary((Map<String, Object>) entry.value, taskLabel, entry.key) : null
        }
    }

    @CompileDynamic
    static MagnitudeSummary buildMagnitudeSummary(Map<String, Object> magnitudeSummaryDataJson, String taskLabel, String magnitudeName) {
        MagnitudeSummary magnitudeSummary = new MagnitudeSummary(name: magnitudeName, taskLabel: taskLabel)
        magnitudeSummaryDataJson.each { String k, def v ->
            if (!isIgnoredField(k, magnitudeSummary)) {
                magnitudeSummary[k] = v
            }
        }

        magnitudeSummary
    }

    private static boolean isIgnoredField(String key, MagnitudeSummary magnitudeSummary) {
        !magnitudeSummary.hasProperty(key)
    }

}
