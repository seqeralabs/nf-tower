package io.seqera.watchtower.service.auth

import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UserDetails
import io.reactivex.Flowable
import org.reactivestreams.Publisher

import javax.inject.Singleton

@Singleton
class AuthenticationProviderEmailLink implements AuthenticationProvider {

    @Override
    Publisher<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
        if (authenticationRequest.identity == "sherlock" && authenticationRequest.secret == "password") {
            return Flowable.just(new UserDetails((String) authenticationRequest.identity, [])) as Publisher<AuthenticationResponse>
        }
        return Flowable.just(new AuthenticationFailed()) as Publisher<AuthenticationResponse>
    }

}
