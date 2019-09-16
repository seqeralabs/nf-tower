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

import javax.inject.Inject
import javax.inject.Singleton

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.context.scope.refresh.RefreshEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.security.authentication.UserDetails
import io.micronaut.security.event.LoginSuccessfulEvent
import io.seqera.mail.MailSpooler
import io.seqera.tower.service.LiveEventsService
/**
 * Object listening for application events
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Singleton
@CompileStatic
class ApplicationListener {

    @Inject
    MailSpooler mailSpooler

    @Inject
    LiveEventsService liveEventsService

    @EventListener
    void onStartup(StartupEvent event) {
        log.info "Application started up"
        mailSpooler.start()
    }

    @EventListener
    void onShutdown(ShutdownEvent event) {
        log.info "Application shutting down"
        mailSpooler.stop()
        liveEventsService.stop()
    }

    @EventListener
    void onConfigRefresh(RefreshEvent event) {
        log.info "Got refresh event: " + event.getSource()
    }

    @EventListener
    void onUserLogin(LoginSuccessfulEvent event) {
        log.info "Login event | user=${fetchUserDetails(event.source)}"
    }

    private String fetchUserDetails(source) {
        if(!source)
            return null
        if( source instanceof UserDetails ) {
            return "user=${source.username}; roles=${source.roles}"
        }
        return "user=${source.toString()} [class=${source.getClass().getName()}]"
    }
}
