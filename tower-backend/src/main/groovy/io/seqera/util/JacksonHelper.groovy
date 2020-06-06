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

package io.seqera.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
/**
 * Helper class to handle JSON rendering and parsing
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class JacksonHelper {

    static private ObjectMapper MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

    /**
     * Converts a JSON string to the parameter object
     *
     * @param str A json formatted string representing a job config object
     * @return A concrete instance of {@code T}
     */
    static <T> T fromJson(String str, Class<T> type) {
        str != null ? MAPPER.readValue(str, type) : null
    }

    /**
     * Converts a concrete instance of of {@code T} to a json
     * representation
     *
     * @param config A concrete instance of of {@code T}
     * @return A json representation of the specified object
     */
    static String toJson(Object config) {
        config != null ? MAPPER.writeValueAsString(config) : null
    }
}
