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

package io.seqera.watchtower

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.info.*;

/**
 * Application entry-point
 */
@OpenAPIDefinition(
    info = @Info(
            title = "watchtower-service",
            version = "0.0"
    )
)
class Application {
    static void main(String[] args) {
        Micronaut.run(Application)
    }
}