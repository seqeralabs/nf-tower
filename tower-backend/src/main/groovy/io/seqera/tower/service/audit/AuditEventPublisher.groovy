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

import javax.inject.Inject
import javax.inject.Singleton

import groovy.transform.CompileStatic
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.http.server.util.HttpClientAddressResolver
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.utils.SecurityService
/**
 * Creates and publish application audit events
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Singleton
@CompileStatic
class AuditEventPublisher {

    @Inject SecurityService securityService
    @Inject ApplicationEventPublisher eventPublisher
    @Inject HttpClientAddressResolver addressResolver

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

    void userSignIn(String userId) {
        final address = getClientAddress()
        final event = new AuditEvent(
                clientIp:address,
                type: AuditEventType.user_sign_in,
                principal: userId,
                target: userId )

        eventPublisher.publishEvent(event)
    }
}
