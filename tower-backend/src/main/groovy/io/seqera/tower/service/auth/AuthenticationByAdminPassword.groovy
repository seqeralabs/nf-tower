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

import javax.annotation.Nullable
import javax.inject.Inject

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UserDetails
import io.reactivex.Flowable
import io.seqera.tower.service.UserService
import org.reactivestreams.Publisher
/**
 * Allow auth admin user for management purpose
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class AuthenticationByAdminPassword implements AuthenticationProvider {

    @Value('${tower.admin.user:`admin`}')
    @Nullable
    String adminUsername

    @Value('${tower.admin.password:``}')
    @Nullable
    String adminPassword

    private UserService userService

    @Inject
    @Client('/')
    RxHttpClient httpClient

    @Inject
    AuthenticationProviderByAuthToken(UserService userService) {
        this.userService = userService
    }

    @Override
    Publisher<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        final result = authenticate0((String) request.identity, (String) request.secret)
        return Flowable.just(result) as Publisher<AuthenticationResponse>
    }

    protected AuthenticationResponse authenticate0(String identity, String secret) {
        if( !adminUsername || !adminPassword ) {
            return new AuthFailure()
        }

        if( adminUsername!=identity ) {
            return new AuthFailure()
        }


        if ( !isValidSecret(secret) ) {
            // a more explanatory message should be returned
            return new AuthFailure("Unknow user with identity: $identity")
        }

        List<String> authorities = ['ADMIN']
        return new UserDetails(identity, authorities)
    }

    protected boolean isValidSecret(String secret) {
        secret == adminPassword
    }

}
