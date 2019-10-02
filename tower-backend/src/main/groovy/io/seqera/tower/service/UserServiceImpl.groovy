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

package io.seqera.tower.service

import static io.seqera.tower.domain.AccessToken.*

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ValidationException
import java.security.Principal
import java.time.Instant
import java.time.OffsetDateTime

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.seqera.tower.domain.AccessToken
import io.seqera.tower.domain.Role
import io.seqera.tower.domain.User
import io.seqera.tower.domain.UserRole
import io.seqera.tower.domain.Workflow
import io.seqera.tower.exceptions.NonExistingUserException
import io.seqera.util.StringUtils
import io.seqera.util.TokenHelper
import org.springframework.validation.FieldError

@Slf4j
@Singleton
@Transactional
@CompileStatic
class UserServiceImpl implements UserService {

    WorkflowService workflowService

    @Inject
    @Client("/")
    RxHttpClient httpClient

    @Nullable
    @Value('${tower.trusted-emails}')
    List<String> trustedEmails

    UserServiceImpl() { }

    @Inject
    UserServiceImpl(WorkflowService workflowService) {
        this.workflowService = workflowService
    }

    @Override
    List<User> list(int offset=0, int max=100) {
        User.list(offset:offset, max:max)
    }

    @CompileDynamic
    User findByEmailAndAuthToken(String email, String token) {
        User.findByEmailAndAuthToken(email, token, [fetch: [accessTokens: 'join']])
    }

    @CompileDynamic
    User findByUserNameAndAccessToken(String userName, String token) {
        new DetachedCriteria<User>(User).build {
            accessTokens {
                eq('token', token)
            }
        }.get()
    }

    @CompileDynamic
    List<String> findAuthoritiesByEmail(String email) {
        User user = User.findByEmail(email)

        findAuthoritiesOfUser(user)
    }

    @CompileDynamic
    List<String> findAuthoritiesOfUser(User user) {
        List<UserRole> rolesOfUser = UserRole.findAllByUser(user)

        return rolesOfUser.role.authority
    }

    protected String makeUserNameFromEmail(String email) {
        def result = email.toLowerCase().replaceAll(/@.*/, '')
        result = result.replaceAll(/[^a-z\d]/,'-')
        int p
        while( (p=result.indexOf('--'))!=-1 )
            result = result.substring(0,p) + result.substring(p+1, result.size())

        if( result.startsWith('-') )
            result = result.substring(1)

        if( result.endsWith('-') )
            result = result.substring(0,result.size()-1)

        if( result.size() > 20 )
            result = result.substring(0,20)

        return result
    }

    @CompileDynamic
    protected String checkUniqueName(final String userName ) {
        int count=0
        String result = userName
        while (User.countByUserName(result)) {
            result = "${userName}${++count}"
            if( count > 100 )
                throw new IllegalStateException("Too many userName check tentatives: $userName")
        }

        return result
    }


    @CompileDynamic
    User create(String email, String authority) {
        // create the user name starting from the email user name
        String userName = makeUserNameFromEmail(email)
        userName = checkUniqueName(userName)

        Role role = Role.findByAuthority(authority) ?: createRole(authority)

        User user = new User(email: email, userName: userName)
        user.trusted = isTrustedEmail(email)
        if( user.trusted ) {
            user.authTime = Instant.now()
            user.authToken = TokenHelper.createHexToken()
        }

        user.addToAccessTokens(new AccessToken(token: TokenHelper.createHexToken(), name: DEFAULT_TOKEN, dateCreated: Instant.now()))
        user.save()

        UserRole userRole = new UserRole(user: user, role: role)
        userRole.save()

        // Try to get a gravatar avatar URL
        user.avatar = getAvatarUrl(email)

        checkUserSaveErrors(user)

        return user
    }


    protected String getAvatarUrl(String email) {
        assert email
        try {
            final emailHash = email.trim().toLowerCase().md5()
            final url = "https://www.gravatar.com/avatar/${emailHash}?d=404"

            // make an http request to probe if the avatar exists
            final resp = httpClient
                    .toBlocking()
                    .exchange(HttpRequest.HEAD(url))

            return resp.status() == HttpStatus.OK ? url : null
        }
        catch (Exception e) {
            log.error("Couldn't fetch Gravatar for email=$email | ${e.message}")
            return null
        }
    }

    protected boolean isTrustedEmail(String email) {
        if( trustedEmails==null ) {
            // implicitly trusted if no rule is specified
            return true
        }
        
        for( String pattern : trustedEmails ) {
            if( StringUtils.like(email, pattern) )
                return true
        }
        return false
    }


    @CompileDynamic
    @Override
    User updateUserAuthToken(User user) {
        user.authTime = Instant.now()
        user.authToken = TokenHelper.createHexToken()
        return user.save()
    }

    @CompileDynamic
    private Role createRole(String authority) {
        Role role = new Role(authority: authority)
        role.save()

        return role
    }

    @CompileDynamic
    private void checkUserSaveErrors(User user) {
        if (!user.hasErrors()) {
            return
        }

        List<FieldError> fieldErrors = user.errors.fieldErrors

        FieldError nullableError = fieldErrors.find { it.code == 'nullable' }
        if (nullableError) {
            throw new ValidationException("Can't save a user without ${nullableError.field}")
        }

        FieldError uniqueError = fieldErrors.find { it.code == 'unique' }
        if (uniqueError) {
            throw new ValidationException("Can't save a user with the same ${uniqueError.field} of another")
        }

        FieldError emailError = fieldErrors.find { it.code == 'email.invalid' }
        if (emailError) {
            throw new ValidationException("Can't save a user with bad ${emailError.field} format")
        }

        FieldError urlError = fieldErrors.find { it.code == 'url.invalid' }
        if (urlError) {
            throw new ValidationException("Can't save a user with bad ${urlError.field} URL format")
        }

        List<String> uncustomizedErrors = fieldErrors.collect { "${it.field}|${it.code}".toString() }
        throw new ValidationException("Can't save user. Validation errors: ${uncustomizedErrors}")
    }

    @CompileDynamic
    User getFromAuthData(Principal userSecurityData) {
        User.findByEmail(userSecurityData.name)
    }

    @CompileDynamic
    User update(User existingUser, User updatedUserData) {
        if (!existingUser) {
            throw new NonExistingUserException("The user to update doesn't exist")
        }

        existingUser.email = updatedUserData.email
        existingUser.userName = updatedUserData.userName
        existingUser.firstName = updatedUserData.firstName
        existingUser.lastName = updatedUserData.lastName
        existingUser.organization = updatedUserData.organization
        existingUser.description = updatedUserData.description
        existingUser.avatar = updatedUserData.avatar

        existingUser.save()
        checkUserSaveErrors(existingUser)

        existingUser
    }

    @CompileDynamic
    void delete(User existingUser) {
        if (!existingUser) {
            throw new NonExistingUserException("The user to delete doesn't exist")
        }

        UserRole.findAllByUser(existingUser)*.delete()
        workflowService.listByOwner(existingUser, null, null, null).each { Workflow workflow ->
            workflowService.delete(workflow)
        }

        existingUser.delete()
    }

    @CompileDynamic
    User getByAccessToken(String token) {
        new DetachedCriteria<User>(User).build {
            accessTokens {
                eq('token', token)
            }
        }.get()
    }

    User updateLastAccessTime(User user) {
        user.lastAccess = OffsetDateTime.now()
        user.save()
    }

}
