package io.seqera.watchtower.service.auth

import io.micronaut.security.authentication.providers.AuthoritiesFetcher
import io.reactivex.Flowable
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
        Flowable.just(userService.findAuthoritiesByUsername(username))
    }

}
