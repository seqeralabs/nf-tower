package io.seqera.watchtower.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.rules.SecurityRule
import io.seqera.watchtower.domain.auth.User
import io.seqera.watchtower.service.TraceService
import io.seqera.watchtower.service.auth.UserService

import javax.inject.Inject

@Controller("/user")
class UserController {

    UserService userService

    @Inject
    UserController(UserService userService) {
        this.userService = userService
    }


    @Post("/register")
    @Produces(MediaType.TEXT_PLAIN)
    @Secured(SecurityRule.IS_ANONYMOUS)
    HttpResponse<String> register(@Body UsernamePasswordCredentials credentials) {
        try {
            userService.register(credentials.username)

            HttpResponse.ok('User registered!')
        } catch (Exception e) {
            HttpResponse.badRequest(e.message)
        }
    }


}
