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

package io.seqera.tower

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.runtime.Micronaut
import io.seqera.tower.util.RuntimeInfo
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License

/**
 * Tower entry-point
 */
@OpenAPIDefinition(
    info = @Info(
            title = "Nextflow Tower",
            version = "20.06.0",
            description = "Nextflow Tower service API",
            license = @License(name = "MPL 2.0", url = "https://www.mozilla.org/en-US/MPL/2.0/"),
            contact = @Contact(url = "https://seqera.io", name = "Paolo Di Tommaso", email = "p@seqera.io")
    )
)
@Slf4j
@CompileStatic
class Application {
    static void main(String[] args) {
        log.info "++ Tower backend starting ++ ${RuntimeInfo.info('; ')}"
        Micronaut.run(Application)
    }
}
