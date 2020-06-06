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

package io.seqera.tower.service.audit


import static io.seqera.mail.MailHelper.getTemplateFile

import javax.inject.Inject
import javax.inject.Singleton
import java.time.format.DateTimeFormatter

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.http.server.util.HttpClientAddressResolver
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.UserDetails
import io.micronaut.security.utils.SecurityService
import io.seqera.tower.domain.Mail
import io.seqera.tower.domain.MailAttachment
import io.seqera.tower.domain.Workflow
import io.seqera.tower.service.mail.MailService

/**
 * Creates and publish application audit events
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Singleton
@CompileStatic
class AuditEventPublisher {

    @Value('${tower.server-url}')
    String serverUrl

    @Inject SecurityService securityService
    @Inject ApplicationEventPublisher eventPublisher
    @Inject HttpClientAddressResolver addressResolver
    @Inject MailService mailService

    protected String getClientAddress() {
        final req = ServerRequestContext.currentRequest()
        req.isPresent() ? addressResolver.resolve(req.get()) : null
    }

    protected String getPrincipal() {
        final auth = securityService.getAuthentication()
        final Authentication principal = auth.isPresent() ? auth.get() : null
        return principal?.getName()
    }

    void workflowCreation(String workflowId) {
        final event = new AuditEvent(
                type: AuditEventType.workflow_created,
                target: workflowId,
                clientIp: getClientAddress(),
                principal: getPrincipal() )

        eventPublisher.publishEvent(event)
    }


    void workflowCompletion(String workflowId) {
        final address = getClientAddress()
        final event = new AuditEvent(
                clientIp:address,
                type: AuditEventType.workflow_completed,
                target: workflowId,
                principal: getPrincipal() )

        eventPublisher.publishEvent(event)
        sendCompletionEmail(workflowId)
    }

    void workflowDeletion(String workflowId) {
        final address = getClientAddress()
        final event = new AuditEvent(
                clientIp:address,
                type: AuditEventType.workflow_deleted,
                target: workflowId,
                principal: getPrincipal() )

        eventPublisher.publishEvent(event)
    }

    void workflowDropped(String workflowId) {
        final event = new AuditEvent(
                type: AuditEventType.workflow_dropped,
                target: workflowId,
                principal: 'system' )

        eventPublisher.publishEvent(event)
    }

    void workflowStatusChangeFromRequest(String workflowId, String status) {
        final event = new AuditEvent(
                type: AuditEventType.workflow_status_changed,
                clientIp: getClientAddress(),
                target: workflowId,
                status: status,
                principal: getPrincipal() )

        eventPublisher.publishEvent(event)
    }

    void workflowStatusChangeBySystem(String workflowId, String status) {
        final event = new AuditEvent(
                type: AuditEventType.workflow_status_changed,
                target: workflowId,
                status: status,
                principal: 'system' )

        eventPublisher.publishEvent(event)
    }


    void accessTokenCreated(Long tokenId) {
        final address = getClientAddress()
        final event = new AuditEvent(
                clientIp: address,
                type: AuditEventType.access_token_created,
                principal: getPrincipal(),
                target: tokenId.toString() )

        eventPublisher.publishEvent(event)
    }

    void accessTokenDeleted(tokenId) {
        final address = getClientAddress()
        final event = new AuditEvent(
                clientIp:address,
                type: AuditEventType.access_token_deleted,
                principal: getPrincipal(),
                target: tokenId.toString() )

        eventPublisher.publishEvent(event)
    }


    void userCreated(Long userId) {
        final address = getClientAddress()
        final event = new AuditEvent(
                clientIp:address,
                type: AuditEventType.user_created,
                target: userId.toString(),
                principal: getPrincipal() )

        eventPublisher.publishEvent(event)
    }

    void userUpdated(Long userId) {
        final address = getClientAddress()
        final event = new AuditEvent(
                clientIp:address,
                type: AuditEventType.user_updated,
                target: userId.toString(),
                principal: getPrincipal() )

        eventPublisher.publishEvent(event)
    }

    void userDeleted(Long userId) {
        final address = getClientAddress()
        final event = new AuditEvent(
                clientIp:address,
                type: AuditEventType.user_deleted,
                target: userId.toString(),
                principal: getPrincipal() )

        eventPublisher.publishEvent(event)
    }

    void userSignIn(UserDetails details) {
        if( !details ) {
            log.warn ("Missing login event user details")
            return
        }

        final attrs = details.getAttributes('roles','username')
        final login = attrs.get('preferred_username')
        String authId = attrs.get('oauth2Provider')
        if( authId && login )
            authId += '/' + login

        final address = getClientAddress()
        final userId = details.username
        final event = new AuditEvent(
                clientIp:address,
                type: AuditEventType.user_sign_in,
                principal: userId,
                target: userId,
                status: authId )

        log.debug "User sign in event=$event"
        eventPublisher.publishEvent(event)
    }


    /**
     * Send an email notification the user when the workflow execution completes
     *
     * @param workflowId The workflow ID
     */
    void sendCompletionEmail(String workflowId) {
        try {
            sendCompletionEmail0(workflowId)
        }
        catch (Exception e) {
            log.error("Unexpected error while sending completion email for workflow Id=$workflowId", e)
        }
    }

    void sendCompletionEmail0(String workflowId) {
        final workflow = Workflow.get(workflowId)
        if( !workflow ) {
            log.warn "Unknown workflow Id=$workflowId -- ignore notification event"
            return
        }

        if( !workflow.checkIsComplete() ) {
            log.warn "Illegal completion status workflow Id=$workflowId -- ignore notification event"
            return
        }

        if( workflow.owner.notification ) {
            final mail = buildCompletionEmail(workflow)
            mailService.sendMail(mail)
        }
    }

    /**
     * Create the {@link Mail} object representing the notification message
     * to deliver to the user
     * @param workflow The {@link Workflow} object about which notify the completion event
     * @return The {@link Mail} object to be sent.
     */
    protected Mail buildCompletionEmail(Workflow workflow) {
        // create template binding
        def binding = new HashMap(5)
        binding.put('workflow', workflow)
        binding.put('duration_str', parseDuration(workflow.duration ?: 0))
        binding.put('launch_time_str', workflow.start ? DateTimeFormatter.ofPattern('dd-MMM-yyyy HH:mm:ss').format(workflow.start) : '-')
        binding.put('ending_time_str', workflow.complete ? DateTimeFormatter.ofPattern('dd-MMM-yyyy HH:mm:ss').format(workflow.complete) : '-')
        binding.put('server_url', serverUrl)

        Mail mail = new Mail()
        mail.to(workflow.owner.email)
        mail.subject("Workflow completion [${workflow.runName}] - ${workflow.success ? 'SUCCEED' : 'FAILED'}!")
        mail.text(getTemplateFile('/io/seqera/tower/service/workflow-notification.txt', binding))
        mail.body(getTemplateFile('/io/seqera/tower/service/workflow-notification.html', binding))
        mail.attach(MailAttachment.resource('/io/seqera/tower/service/tower-logo.png', contentId: '<tower-logo>', disposition: 'inline'))
        return mail
    }


    private String parseDuration(long durationInMillis) {

        // just prints the milliseconds
        if( durationInMillis < 1_000 ) {
            return durationInMillis + 'ms'
        }

        // when less than 60 seconds round up to 100th of millis
        if( durationInMillis < 60_000 ) {
            return String.valueOf( Math.round(durationInMillis / 1_000 * 10 as float) / 10 ) + 's'
        }

        def secs
        def mins
        def hours
        def days
        def result = []

        // round up to seconds
        secs = Math.round( (double)(durationInMillis / 1_000) )

        mins = secs.intdiv(60)
        secs = secs % 60
        if( secs )
            result.add( secs+'s' )

        hours = mins.intdiv(60)
        mins = mins % 60
        if( mins )
            result.add(0, mins+'m' )

        days = hours.intdiv(24)
        hours = hours % 24
        if( hours )
            result.add(0, hours+'h' )

        if( days )
            result.add(0, days+'d')

        return result.join(' ')
    }
}
