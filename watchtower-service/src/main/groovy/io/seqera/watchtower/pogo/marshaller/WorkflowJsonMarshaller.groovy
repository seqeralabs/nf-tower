package io.seqera.watchtower.pogo.marshaller

import com.fasterxml.jackson.databind.ObjectMapper
import io.seqera.watchtower.domain.MagnitudeSummary
import io.seqera.watchtower.domain.Workflow

class WorkflowJsonMarshaller {

    static Map generateJson(Workflow workflow) {
        Map json = [workflow: [workflowId: workflow.id, manifest: [:], nextflow: [:], stats: [:]], progress: [:]]

        populateJsonWithProperties(workflow, json)
        populateJsonWithMagnitudeSummary(workflow.magnitudeSummaries, json)

        json
    }

    private static void populateJsonWithProperties(Workflow workflow, Map json) {
        Workflow.gormPersistentEntity.persistentPropertyNames.each { String propertyName ->
            def value = workflow[propertyName]

            if (isNextflowField(propertyName)) {
                json.workflow['nextflow'][propertyName.replaceFirst(/nextflow/, '').uncapitalize()] = value
            } else if (isManifestField(propertyName)) {
                json.workflow['manifest'][propertyName.replaceFirst(/manifest/, '').uncapitalize()] = value
            } else if (isMultivalueField(propertyName)) {
                json.workflow[propertyName] = value ? new ObjectMapper().readValue((String) value, Object.class) : null
            } else if (isStatsField(propertyName)) {
                json.workflow['stats'][propertyName] = value
            } else if (isProgressSummaryField(propertyName)) {
                json.progress[propertyName] = value
            } else {
                json.workflow[propertyName] = value
            }
        }
    }

    private static void populateJsonWithMagnitudeSummary(Collection<MagnitudeSummary> magnitudeSummaries, Map json) {
        json.summary = MagnitudeSummaryJsonMarshaller.generateJsonForList(magnitudeSummaries)
    }

    private static boolean isManifestField(String propertyName) {
        propertyName.startsWith('manifest')
    }

    private static boolean isNextflowField(String propertyName) {
        propertyName.startsWith('nextflow')
    }

    private static boolean isMultivalueField(String propertyName) {
        (propertyName == 'configFiles') || (propertyName == 'params')
    }

    private static boolean isProgressSummaryField(String propertyName) {
        (propertyName == 'running') || (propertyName == 'submitted') || (propertyName == 'failed') || (propertyName == 'pending') || (propertyName == 'succeeded') || (propertyName == 'cached')
    }

    private static boolean isStatsField(String propertyName) {
        (propertyName == 'computeTimeFmt') || (propertyName == 'cachedCount') || (propertyName == 'cachedDuration') || (propertyName == 'failedDuration') || (propertyName == 'succeedDuration') || (propertyName == 'failedCount') ||
        (propertyName == 'cachedPct') || (propertyName == 'cachedCountFmt') || (propertyName == 'succeedCountFmt') || (propertyName == 'failedPct') || (propertyName == 'failedCountFmt') || (propertyName == 'ignoredCountFmt') ||
        (propertyName == 'ignoredCount') || (propertyName == 'succeedPct') || (propertyName == 'succeedCount') || (propertyName == 'ignoredPct')
    }

}
