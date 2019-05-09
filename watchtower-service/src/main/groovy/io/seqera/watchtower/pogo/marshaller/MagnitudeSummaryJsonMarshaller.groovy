package io.seqera.watchtower.pogo.marshaller

import io.seqera.watchtower.domain.MagnitudeSummary

class MagnitudeSummaryJsonMarshaller {

    static Map generateJsonForList(Collection<MagnitudeSummary> magnitudeSummaries) {
        Map json = [:]

        magnitudeSummaries.each { MagnitudeSummary magnitudeSummary ->
            json[magnitudeSummary.taskLabel] = json[magnitudeSummary.taskLabel] ?: [:]
            json[magnitudeSummary.taskLabel][magnitudeSummary.name] = generateJson(magnitudeSummary)
        }

        json
    }

    private static Map generateJson(MagnitudeSummary magnitudeSummary) {
        Map json = [:]

        MagnitudeSummary.gormPersistentEntity.persistentPropertyNames.each { String propertyName ->
            if (!isIgnoredField(propertyName)) {
                json[propertyName] = magnitudeSummary[propertyName]
            }
        }

        json
    }

    private static boolean isIgnoredField(String propertyName) {
        (propertyName == 'workflow')
    }

}
