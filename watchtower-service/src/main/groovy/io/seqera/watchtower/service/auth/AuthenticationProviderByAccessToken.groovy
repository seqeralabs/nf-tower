package io.seqera.watchtower.service.auth

import io.micronaut.context.annotation.Value
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UserDetails
import io.reactivex.Flowable
import io.seqera.watchtower.domain.User
import io.seqera.watchtower.service.UserService
import org.reactivestreams.Publisher

import javax.inject.Inject
import javax.inject.Singleton

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
            // a more explanatory message should be returned
            return new AuthFailure('Missing user identity')
        }

        User user = userService.findByUserNameAndAccessToken(identity, token)

        if( !user ) {
            // a more explanatory message should be returned
            return new AuthFailure("Unknow user with identity: $identity")
        }

        List<String> authorities = userService.findAuthoritiesOfUser(user)
        return new UserDetails(user.email, authorities)
    }
}
