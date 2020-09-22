package io.seqera.tower.controller

import groovy.util.logging.Slf4j
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.seqera.tower.service.token.TokenGeneratorService

import javax.inject.Inject

@Slf4j
@Controller("/token")
class TokenGeneratorController extends BaseController {

    @Inject
    TokenGeneratorService tokenGeneratorService

    @Get('/')
    @Secured(SecurityRule.IS_ANONYMOUS)
    HttpResponse<String> index(HttpRequest request) {
        try {
            return HttpResponse.ok(tokenGeneratorService.generateRandomId())

        } catch (Exception e) {
            log.error("Error while generating new token: ${e.message}", e)
            HttpResponse.badRequest(e.message)
        }
    }
}
