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

package io.seqera.tower.events

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Singleton

import groovy.util.logging.Slf4j
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.context.scope.refresh.RefreshEvent
import io.micronaut.security.authentication.UserDetails
import io.micronaut.security.event.LoginSuccessfulEvent
import io.seqera.tower.service.audit.AuditEvent
import io.seqera.tower.service.audit.AuditEventPublisher
import io.seqera.tower.service.audit.AuditService
import io.seqera.tower.service.cron.CronService
/**
 * Implements application events dispatching logic
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Singleton
class ApplicationEventDispatcherImpl implements ApplicationEventDispatcher {

    @Inject AuditEventPublisher eventPublisher
    @Inject @Nullable CronService cronService
    @Inject @Nullable AuditService auditService

    void onStartup(StartupEvent event) {
        log.info "Application started up"
        // cron
        if( cronService )
            cronService.start()
        else
            log.info "Cron service NOT configured"
    }

    void onShutdown(ShutdownEvent event) {
        log.info "Application shutting down"
        cronService?.stop()
    }

    void onConfigRefresh(RefreshEvent event) {
        log.info "Got refresh event: " + event.getSource()
    }

    void onUserLogin(LoginSuccessfulEvent event) {
        try {
            eventPublisher.userSignIn((UserDetails)event.source)
        }
        catch (Exception e) {
            log.error "Unable to process user sign-in audit event | ${e.message ?: e}"
        }
    }

    void onAuditEvent(AuditEvent event) {
        log.trace "Saving audit event=$event [service=${auditService?.class?.getSimpleName()}]"
        try {
            auditService?.save(event)
        }
        catch ( Exception e ){
            log.error "Unable to save audit event | ${e.message ?: e}"
        }
    }

}
