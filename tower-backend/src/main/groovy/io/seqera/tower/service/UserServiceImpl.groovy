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

import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ValidationException
import java.security.Principal
import java.time.Instant

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import groovy.text.GStringTemplateEngine
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Value
import io.seqera.mail.Attachment
import io.seqera.mail.Mail
import io.seqera.tower.domain.AccessToken
import io.seqera.tower.domain.Role
import io.seqera.tower.domain.User
import io.seqera.tower.domain.UserRole
import io.seqera.tower.domain.Workflow
import io.seqera.tower.exceptions.NonExistingUserException
import io.seqera.util.TokenHelper
import org.springframework.validation.FieldError

@Singleton
@Transactional
@CompileStatic
class UserServiceImpl implements UserService {


    @Value('${app.name:Nextflow Tower}')
    String appName
    @Value('${front.url}')
    String frontendUrl

    MailService mailService
    WorkflowService workflowService

    UserServiceImpl() {
    }

    @Inject
    UserServiceImpl(MailService mailService, WorkflowService workflowService) {
        this.mailService = mailService
        this.workflowService = workflowService
    }

    @CompileDynamic
    User register(String email) {
        User user = User.findByEmail(email)

        user = user ? updateUserToken(user) : createUser(email, 'ROLE_USER')
        checkUserSaveErrors(user)
        sendAccessEmail(user)

        return user
    }

    protected void sendAccessEmail(User user) {
        assert user.email, "Missing email address for user=$user"

        // create template binding
        def binding = new HashMap(5)
        binding.app_name = appName
        binding.auth_url = buildAccessUrl(user)
        binding.frontend_url = frontendUrl
        binding.user = user.firstName ?: user.userName

        Mail mail = new Mail()
        mail.to(user.email)
        mail.subject("$appName Sign in")
        mail.text(getTextTemplate(binding))
        mail.body(getHtmlTemplate(binding))
        mail.attach(getLogoAttachment())

        mailService.sendMail(mail)
    }

    /**
     * Load and resolve default text email template
     *
     * @return Resolved text template string
     */
    protected String getTextTemplate(Map binding) {
        getTemplateFile('/io/seqera/tower/service/auth-mail.txt', binding)
    }

    /**
     * Load and resolve default HTML email template
     *
     * @return Resolved HTML template string
     */
    protected String getHtmlTemplate(Map binding) {
        getTemplateFile('/io/seqera/tower/service/auth-mail.html', binding)
    }

    /**
     * Load the HTML email logo attachment
     * @return A {@link Attachment} object representing the image logo to be included in the HTML email
     */
    protected Attachment getLogoAttachment() {
        Attachment.resource('/io/seqera/tower/service/seqera-logo.png', contentId: '<seqera-logo>', disposition: 'inline')
    }

    protected String getTemplateFile(String classpathResource, Map binding) {
        def source = this.class.getResourceAsStream(classpathResource)
        if (!source)
            throw new IllegalArgumentException("Cannot load notification default template -- check classpath resource: $classpathResource")
        loadMailTemplate0(source, binding)
    }

    private String loadMailTemplate0(InputStream source, Map binding) {
        def map = new HashMap()
        map.putAll(binding)

        def template = new GStringTemplateEngine().createTemplate(new InputStreamReader(source))
        template.make(map).toString()
    }

    protected String buildAccessUrl(User user) {
        String accessUrl = "${frontendUrl}/auth?email=${URLEncoder.encode(user.email,'UTF-8')}&authToken=${user.authToken}"
        return new URI(accessUrl).toString()
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
        result
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
    private User createUser(String email, String authority) {
        // create the user name starting from the email user name
        String userName = makeUserNameFromEmail(email)
        userName = checkUniqueName(userName)

        Role role = Role.findByAuthority(authority) ?: createRole(authority)

        String authToken = TokenHelper.createHexToken()
        User user = new User(email: email, authToken: authToken, userName: userName, authTime: Instant.now())
        user.addToAccessTokens(new AccessToken(token: TokenHelper.createHexToken(), name: 'default', dateCreated: Instant.now()))
        user.save()

        UserRole userRole = new UserRole(user: user, role: role)
        userRole.save()

        return user
    }

    @CompileDynamic
    private User updateUserToken(User user) {
        user.authTime = Instant.now()
        user.authToken = TokenHelper.createHexToken()
        user.save()
        return user
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
        workflowService.list(existingUser).each { Workflow workflow ->
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
}
