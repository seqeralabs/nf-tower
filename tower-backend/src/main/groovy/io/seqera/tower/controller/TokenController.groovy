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

package io.seqera.tower.controller

import javax.inject.Inject

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.seqera.tower.exceptions.TowerException
import io.seqera.tower.exchange.MessageResponse
import io.seqera.tower.exchange.token.CreateAccessTokenResponse
import io.seqera.tower.exchange.token.ListAccessTokensResponse
import io.seqera.tower.service.AccessTokenService
import io.seqera.tower.service.UserService
/**
 * Implement the controller to handle access token operations
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Controller("/token")
@Transactional
@CompileStatic
@Secured(['ROLE_USER'])
class TokenController  extends BaseController {

    @Inject UserService userService

    @Inject AccessTokenService accessTokenService

    @Get("/list")
    HttpResponse<ListAccessTokensResponse> list(Authentication authentication) {
        try {
            final user = userService.getFromAuthData(authentication)
            final result = accessTokenService.findByUser(user)
            HttpResponse.ok(new ListAccessTokensResponse(tokens: result))
        }
        catch( Exception e ) {
            log.error "Unable to retrieve access tokens for auth=$authentication", e
            HttpResponse.badRequest(new ListAccessTokensResponse(message: "Oops... Failed to retrieve access tokens"))
        }
    }

    @Post("/create")
    HttpResponse<CreateAccessTokenResponse> create(Authentication authentication, String name) {
        try {
            final user = userService.getFromAuthData(authentication)
            final token = accessTokenService.createToken(name, user)
            HttpResponse.ok(new CreateAccessTokenResponse(token: token))
        }
        catch ( TowerException e ) {
            log.debug "Unable to create an access token with name=$name for auth=$authentication"
            HttpResponse.badRequest(new CreateAccessTokenResponse(message: e.message))
        }
        catch( Exception e ) {
            log.error "Unable to create an access token with name=$name for auth=$authentication", e
            HttpResponse.badRequest(new CreateAccessTokenResponse(message: "Oops... Failed to create access token '$name'"))
        }
    }

    @Delete("/delete/{tokenId}")
    HttpResponse delete(Long tokenId) {
        try {
            final count = accessTokenService.deleteById(tokenId)
            return ( count>0 ?
                    HttpResponse.status(HttpStatus.NO_CONTENT):
                    HttpResponse.badRequest(new MessageResponse(("Oops... Failed to delete access token"))) )

        }
        catch( Exception e ) {
            log.error "Unable to delete token with id=$tokenId", e
            HttpResponse.badRequest(new MessageResponse("Oops... Failed to delete access token"))
        }
    }

    @Delete("/delete-all")
    HttpResponse deleteAll(Authentication authentication) {
        try {
            final user = userService.getFromAuthData(authentication)
            final count = accessTokenService.deleteByUser(user)
            return ( count>0 ?
                    HttpResponse.status(HttpStatus.NO_CONTENT):
                    HttpResponse.badRequest(new MessageResponse("Oops... Failed to revoke all access token")))
        }
        catch( Exception e ) {
            log.error "Unable to delete all tokens for auth=$authentication", e
            HttpResponse.badRequest(new MessageResponse(("Oops... Failed to delete access tokens")))
        }
    }

}
