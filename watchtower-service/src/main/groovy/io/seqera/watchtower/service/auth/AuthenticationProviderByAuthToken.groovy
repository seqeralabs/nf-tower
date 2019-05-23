package io.seqera.watchtower.service.auth

import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UserDetails
import io.reactivex.Flowable
import io.seqera.watchtower.domain.auth.User
import org.reactivestreams.Publisher

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationProviderByAuthToken implements AuthenticationProvider {

    private UserService userService

    @Inject
    AuthoritiesFetcherService(UserService userService) {
        this.userService = userService
    }

    @Override
    Publisher<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
        User user = userService.findByEmailAndAuthToken((String) authenticationRequest.identity, (String) authenticationRequest.secret)
        if (user) {
            List<String> authorities = userService.findAuthoritiesByEmail(user.email)

            Map attributes = [username: user.username, email: user.email, firstName: user.firstName, lastName: user.lastName, organisation: user.organization, description: user.description, avatar: user.avatar, authToken: user.authToken]
            return Flowable.just(new UserDetails(user.email, authorities, (Map) attributes)) as Publisher<AuthenticationResponse>
        }

        return Flowable.just(new AuthenticationFailed()) as Publisher<AuthenticationResponse>
    }

}
