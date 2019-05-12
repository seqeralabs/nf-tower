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