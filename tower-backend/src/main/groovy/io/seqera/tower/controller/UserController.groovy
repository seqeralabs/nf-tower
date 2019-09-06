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

import javax.annotation.Nullable
import javax.inject.Inject

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.seqera.tower.domain.User
import io.seqera.tower.exchange.user.DeleteUserResponse
import io.seqera.tower.exchange.user.EnableUserResponse
import io.seqera.tower.exchange.user.GetUserResponse
import io.seqera.tower.exchange.user.ListUserResponse
import io.seqera.tower.service.GateService
import io.seqera.tower.service.UserService

@Slf4j
@Controller("/user")
@Secured(SecurityRule.IS_AUTHENTICATED)
class UserController extends BaseController {

    @Inject
    UserService userService
    
    @Inject GateService gateService

    @Post("/update")
    @Produces(MediaType.TEXT_PLAIN)
    HttpResponse<String> update(@Body User userData, Authentication authentication) {
        try {
            userService.update(userService.getFromAuthData(authentication), userData)

            HttpResponse.ok('User successfully updated!')
        } catch (Exception e) {
            log.error("Failure on user update: ${e.message}", e)
            HttpResponse.badRequest(e.message)
        }
    }

    @Delete("/delete")
    @Produces(MediaType.TEXT_PLAIN)
    HttpResponse<String> delete(Authentication authentication) {
        try {
            userService.delete(userService.getFromAuthData(authentication))

            HttpResponse.ok('User successfully deleted!')
        } catch (Exception e) {
            log.error("Failure on user delete: ${e.message}", e)
            HttpResponse.badRequest(e.message)
        }
    }

    @Delete("/delete/{userId}")
    @Secured(['ADMIN'])
    @Transactional
    HttpResponse<DeleteUserResponse> delete(Long userId) {
        User user = User.get(userId)
        if( !user )
            return HttpResponse.badRequest(new DeleteUserResponse(message: "Cannot find user with ID=$userId"))

        userService.delete(user)
        HttpResponse.ok(new DeleteUserResponse(message: 'OK'))
    }

    @Get('/list{?max,offset}')
    HttpResponse<ListUserResponse> list(@Nullable Integer offset, @Nullable Integer max) {
        if( offset==null ) offset=0
        if( max==null ) max=100
        if( max>1000 )
            return HttpResponse.badRequest(new ListUserResponse(message: "Cannot retried more than 1000 users"))

        try {
            def users = userService.list(offset, max)
            HttpResponse.ok(new ListUserResponse(users: users))
        }
        catch( Exception  e ) {
            log.error("Failed to list users", e)
            HttpResponse.badRequest(new ListUserResponse(message:"Can't retried users list"))
        }
    }

    @Get('/get/{userId}')
    @Secured(['ADMIN'])
    @Transactional
    HttpResponse<GetUserResponse> get(Long userId) {
        final user = User.get(userId)
        if( !user )
            return HttpResponse.badRequest(new GetUserResponse(message: "Cannot find user with ID=$userId"))

        HttpResponse.ok(new GetUserResponse(user:user))
    }


    @Get('/allow/login/{userId}')
    @Secured(['ADMIN'])
    @Transactional
    HttpResponse<EnableUserResponse> allowLogin(Long userId) {
        final user = User.get(userId)
        if( !user )
            return HttpResponse.badRequest(new EnableUserResponse(message: "Cannot find user with ID=$userId"))

        gateService.allowLogin(user)
        HttpResponse.ok(new EnableUserResponse(message: 'OK'))
    }
}
