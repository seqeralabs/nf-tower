package io.seqera.watchtower.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule

@Controller("/user")
@Secured(SecurityRule.IS_ANONYMOUS)
class UserController {


    @Post("/register")
    HttpResponse register() {

    }


}
