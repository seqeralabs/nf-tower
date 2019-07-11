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

import io.micronaut.security.authentication.providers.AuthoritiesFetcher
import io.reactivex.Flowable
import io.seqera.tower.service.UserService
import org.reactivestreams.Publisher

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthoritiesFetcherService implements AuthoritiesFetcher {

    private UserService userService

    @Inject
    AuthoritiesFetcherService(UserService userService) {
        this.userService = userService
    }

    @Override
    Publisher<List<String>> findAuthoritiesByUsername(String username) {
        Flowable.just(userService.findAuthoritiesByEmail(username))
    }

}
