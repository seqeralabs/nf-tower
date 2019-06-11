package io.seqera.watchtower.service.auth

import groovy.transform.CompileStatic
import io.micronaut.security.authentication.AuthenticationFailed

/**
 * Auth response object that allows a custom response message
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class AuthFailure extends AuthenticationFailed {

    private String message

    AuthFailure() {
    }

    AuthFailure(String message) {
        this.message = message
    }

    @Override
    Optional<String> getMessage() {
        return message ? Optional.of(message) : super.getMessage()
    }

}
