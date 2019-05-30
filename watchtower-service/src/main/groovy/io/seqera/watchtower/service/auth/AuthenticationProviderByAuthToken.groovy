package io.seqera.watchtower.service.auth

import javax.inject.Inject
import javax.inject.Singleton
import java.time.Duration
import java.time.Instant

import io.micronaut.context.annotation.Value
import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UserDetails
import io.reactivex.Flowable
import io.seqera.watchtower.domain.User
import io.seqera.watchtower.service.UserService
import org.reactivestreams.Publisher

@Singleton
class AuthenticationProviderByAuthToken implements AuthenticationProvider {

    private UserService userService

    @Value('${auth.mail.duration:30m}')
    Duration authMailDuration 

    @Inject
    AuthenticationProviderByAuthToken(UserService userService) {
        this.userService = userService
    }

    @Override
    Publisher<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
        final result = authenticate0((String)authenticationRequest.identity, (String) authenticationRequest.secret)
        return Flowable.just(result) as Publisher<AuthenticationResponse>
    }

    protected authenticate0(String email, String token) {
        User user = userService.findByEmailAndAuthToken(email, token)
        if (user && !isAuthTokenExpired(user)) {
            List<String> authorities = userService.findAuthoritiesByEmail(user.email)

            Map attributes = [email: user.email, userName: user.userName, firstName: user.firstName, lastName: user.lastName, organization: user.organization, description: user.description, avatar: user.avatar]
            return new UserDetails(user.email, authorities, (Map) attributes)
        }

        return new AuthenticationFailed()
    }

    private boolean isAuthTokenExpired(User user) {
        Duration delta = Duration.between(user.authTime, Instant.now())

        (delta >= authMailDuration)
    }

}
