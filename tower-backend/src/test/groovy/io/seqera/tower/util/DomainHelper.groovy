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

package io.seqera.tower.util

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class DomainHelper {

    static private ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()

    static ObjectMapper getMapper() { mapper }

    static String toJson(Object object) {
        def writer = new StringWriter()
        mapper.writeValue(writer, object)
        return JsonOutput.prettyPrint(writer.toString())
    }

}
