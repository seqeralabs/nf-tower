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

import io.micronaut.context.annotation.Value
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
class AuthenticationByAdminPassword implements AuthenticationProvider {

    @Value('${tower.admin.user:`admin`}')
    String adminUsername

    @Value('${tower.admin.password:``}')
    String adminPassword

    private UserService userService

    @Inject
    AuthenticationProviderByAuthToken(UserService userService) {
        this.userService = userService
    }

    @Override
    Publisher<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        final result = authenticate0((String) request.identity, (String) request.secret)
        return Flowable.just(result) as Publisher<AuthenticationResponse>
    }

    protected AuthenticationResponse authenticate0(String identity, String password) {
       if( !adminUsername || !adminPassword )
           return new AuthFailure()

        final OK = identity==adminUsername && password==adminPassword
        if (!OK) {
            // a more explanatory message should be returned
            return new AuthFailure("Unknow user with identity: $identity")
        }

        List<String> authorities = ['ADMIN']
        return new UserDetails(identity, authorities)
    }

}
