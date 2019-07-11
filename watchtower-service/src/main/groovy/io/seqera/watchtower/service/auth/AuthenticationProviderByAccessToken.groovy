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

package io.seqera.watchtower.service.auth

import javax.inject.Inject
import javax.inject.Singleton

import groovy.util.logging.Slf4j
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UserDetails
import io.reactivex.Flowable
import io.seqera.watchtower.domain.User
import io.seqera.watchtower.service.UserService
import org.reactivestreams.Publisher

@Slf4j
@Singleton
class AuthenticationProviderByAccessToken implements AuthenticationProvider {

    private UserService userService

    @Inject
    AuthenticationProviderByAccessToken(UserService userService) {
        this.userService = userService
    }

    @Override
    Publisher<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
        final result = authenticate0((String)authenticationRequest.identity, (String) authenticationRequest.secret)
        return Flowable.just(result) as Publisher<AuthenticationResponse>
    }

    protected AuthenticationResponse authenticate0(String identity, String token) {
        if( !identity ) {
            log.debug "Missing user identity -- token=$token"
            // a more explanatory message should be returned
            return new AuthFailure('Missing user identity')
        }

        User user = userService.findByUserNameAndAccessToken(identity, token)

        if( !user ) {
            log.debug "Missing user for identity=$identity -- token=$token"
            // a more explanatory message should be returned
            return new AuthFailure("Unknow user with identity: $identity")
        }

        List<String> authorities = userService.findAuthoritiesOfUser(user)
        return new UserDetails(user.email, authorities)
    }
}
