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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.security.authentication.providers.AuthoritiesFetcher
import io.reactivex.Flowable
import io.seqera.tower.service.UserService
import org.reactivestreams.Publisher

@Slf4j
@Singleton
@CompileStatic
class AuthoritiesFetcherService implements AuthoritiesFetcher {

    private UserService userService

    @Inject
    AuthoritiesFetcherService(UserService userService) {
        this.userService = userService
    }

    @Override
    Publisher<List<String>> findAuthoritiesByUsername(String identity) {
        // it can be either email or userId
        final roles = identity.contains('@')
                ? userService.findAuthoritiesByEmail(identity)
                : userService.findAuthoritiesByUid(identity)
        log.debug "Find authority for user=$identity; roles=$roles"
        return Flowable.just(roles)
    }

}
