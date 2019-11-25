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
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UserDetails
import io.reactivex.Flowable
import io.seqera.tower.exchange.captcha.CaptchaVerifyResponse
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

    @Value('${tower.captcha.secret-key}')
    @Nullable
    String captchaSecretKey

    @Value('${tower.admin.user:`admin`}')
    String adminUsername

    @Value('${tower.admin.password:``}')
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
        if( !adminUsername || !adminPassword )
           return new AuthFailure()

        if( adminUsername!=identity )
            return new AuthFailure()

        if ( !isValidSecret(secret) ) {
            // a more explanatory message should be returned
            return new AuthFailure("Unknow user with identity: $identity")
        }

        List<String> authorities = ['ADMIN']
        return new UserDetails(identity, authorities)
    }

    protected boolean isValidSecret(String secret) {
        // when the captcha secret key is not specified in the
        // config file the `secret`is expected to be the plain password
        // the needs to be identical to the one in the config file
        if( !captchaSecretKey || !secret.contains('\t') ) {
            return secret == adminPassword
        }
        // when the captcha key is given the secret is expected to be a tab separated values pair
        // where the first element is the password and the second value is the captcha response string
        else {
            final tokens = secret.tokenize('\t')
            return tokens[0] == adminPassword && validateCaptcha(tokens[1])
        }
    }

    protected boolean validateCaptcha(String captcha) {
        final url = "https://www.google.com/recaptcha/api/siteverify?secret=$captchaSecretKey&response=${captcha}"
        final resp = httpClient.toBlocking().exchange(HttpRequest.POST(url, ''), CaptchaVerifyResponse)
        final result = resp.status() == HttpStatus.OK && resp.body().success
        if( !result ) {
            log.info "Captcha verification failed for user=$adminUsername; resp=${resp.body()}"
        }
        return result
    }

}
