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

import grails.gorm.transactions.Transactional
import groovy.text.GStringTemplateEngine
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.seqera.tower.domain.MailAttachment
import io.seqera.tower.domain.Mail
import io.seqera.tower.domain.User
import io.seqera.tower.exchange.gate.AccessGateResponse
/**
 * Implements the Gate services
 *
 * See {@link GateService}
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Singleton
@Transactional
@CompileStatic
class GateServiceImpl implements GateService {

    @Value('${tower.app-name}')
    String appName

    @Value('${tower.server-url}')
    String serverUrl

    @Value('${tower.contact-email}')
    String contactEmail


    @Inject
    UserService userService

    @Inject
    MailService mailService

    @CompileDynamic
    @Override
    AccessGateResponse access(String email) {
        final result = new AccessGateResponse()
        result.user = User.findByEmail(email)

        final isNew = result.user==null
        if( isNew ) {
            result.user = userService.create(email, 'ROLE_USER')
        }

        if( result.user.trusted ) {
            // if the user is trusted send the login email
            result.user = userService.generateAuthToken(result.user)
            result.state = AccessGateResponse.State.LOGIN_ALLOWED
            sendLoginEmail(result.user)
        }
        else if( isNew ) {
            // otherwise notify the system admin of a new user
            result.state = AccessGateResponse.State.PENDING_APPROVAL
            sendNewUserEmail(result.user)
        }
        else {
            result.state = AccessGateResponse.State.KEEP_CALM_PLEASE
        }

        return result
    }


    protected void sendLoginEmail(User user) {
        assert user.email, "Missing email address for user=$user"

        final mail = buildAccessEmail(user)
        mailService.sendMail(mail)
    }

    protected void sendNewUserEmail(User user) {
        assert user.email, "Missing email address for user=$user"
        mailService.sendMail( buildNewUserEmail(user) )
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
     * @return A {@link MailAttachment} object representing the image logo to be included in the HTML email
     */
    protected MailAttachment getLogoAttachment() {
        MailAttachment.resource('/io/seqera/tower/service/tower-logo.png', contentId: '<tower-logo>', disposition: 'inline')
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
        String accessUrl = "${serverUrl}/auth?email=${URLEncoder.encode(user.email,'UTF-8')}&authToken=${user.authToken}"
        return new URI(accessUrl).toString()
    }

    protected String getEnableUrl(String server, userId) {
        if( server.contains('localhost'))
            return 'http://localhost:8001/user?id=' + userId
        final url = new URL(server)
        return "${url.protocol}://admin.${url.host}/user?id=${userId}"
    }

    protected Mail buildAccessEmail(User user) {
        // create template binding
        def binding = new HashMap(5)
        binding.app_name = appName
        binding.auth_url = buildAccessUrl(user)
        binding.server_url = serverUrl
        binding.user = user.firstName ?: user.userName

        Mail mail = new Mail()
        mail.to(user.email)
        mail.subject("$appName Sign in")
        mail.text(getTextTemplate(binding))
        mail.body(getHtmlTemplate(binding))
        mail.attach(getLogoAttachment())
        return mail
    }

    protected Mail buildNewUserEmail(User user) {
        def binding = new HashMap(5)
        binding.app_name = appName
        binding.server_url = serverUrl
        binding.user_name = user.userName
        binding.user_email = user.email
        binding.user_id = user.id
        binding.enable_url = getEnableUrl(serverUrl, user.id)

        Mail mail = new Mail()
        mail.to(contactEmail)
        mail.subject("New user registration")
        mail.text(getTemplateFile('/io/seqera/tower/service/new-user-mail.txt', binding))
        mail.body(getTemplateFile('/io/seqera/tower/service/new-user-mail.html', binding))
        mail.attach(getLogoAttachment())
        return mail
    }


    protected Mail buildWelcomeEmail(User user) {
        // create template binding
        def binding = new HashMap(5)
        binding.server_url = serverUrl
        binding.login_url = "${serverUrl}/login"
        binding.user_email = user.email

        Mail mail = new Mail()
        mail.to(user.email)
        mail.subject("You now have beta access to Nextflow Tower!")
        mail.text(getTemplateFile('/io/seqera/tower/service/welcome-mail.txt', binding))
        mail.body(getTemplateFile('/io/seqera/tower/service/welcome-mail.html', binding))
        mail.attach(MailAttachment.resource('/io/seqera/tower/service/tower-splash.png', contentId: '<tower-splash>', disposition: 'inline'))
        return mail
    }

    void allowLogin(User user) {
        user.trusted = true
        user.save(failOnError:true)

        // send welcome email
        final welcome = buildWelcomeEmail(user)
        mailService.sendMail(welcome)
    }
}
