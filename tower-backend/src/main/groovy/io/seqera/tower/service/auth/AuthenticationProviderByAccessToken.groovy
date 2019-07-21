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

package io.seqera.tower.service.auth

import javax.inject.Inject
import javax.inject.Singleton

import groovy.util.logging.Slf4j
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UserDetails
import io.reactivex.Flowable
import io.seqera.tower.domain.User
import io.seqera.tower.service.UserService
import org.reactivestreams.Publisher

@Slf4j
@Singleton
class AuthenticationProviderByAccessToken implements AuthenticationProvider {

    public static final ID = '@token'

    private UserService userService

    @Inject
    AuthenticationProviderByAccessToken(UserService userService) {
        this.userService = userService
    }

    @Override
    Publisher<AuthenticationResponse> authenticate(AuthenticationRequest req) {
        if( req.identity != ID ) {
            log.debug "Not a valid access token identity=$req.identity"
            // a more explanatory message should be returned
            def result = new AuthFailure('Not a valid access token identify')
            return Flowable.just(result) as Publisher<AuthenticationResponse>
        }

        final result = authToken0((String)req.secret)
        return Flowable.just(result) as Publisher<AuthenticationResponse>
    }

    protected AuthenticationResponse authToken0(String token) {

        User user = userService.getByAccessToken(token)

        if( !user ) {
            log.info "Missing user with token=$token"
            // a more explanatory message should be returned
            return new AuthFailure("Unknow user with token: $token")
        }

        List<String> authorities = userService.findAuthoritiesOfUser(user)
        return new UserDetails(user.email, authorities)
    }
}
