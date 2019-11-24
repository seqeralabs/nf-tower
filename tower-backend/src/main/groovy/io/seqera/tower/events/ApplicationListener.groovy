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
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.context.scope.refresh.RefreshEvent
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.security.event.LoginSuccessfulEvent
import io.seqera.tower.service.audit.AuditEvent
/**
 * Object listening for application events
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Singleton
@CompileStatic
class ApplicationListener {

    @Inject
    ApplicationEventDispatcher dispatcher

    @EventListener
    void onStartup(StartupEvent event) {
        dispatcher.onStartup(event)
    }

    @EventListener
    void onShutdown(ShutdownEvent event) {
        dispatcher.onShutdown(event)
    }

    @EventListener
    void onConfigRefresh(RefreshEvent event) {
        dispatcher.onConfigRefresh(event)
    }

    @EventListener
    void onUserLogin(LoginSuccessfulEvent event) {
        dispatcher.onUserLogin(event)
    }

    @EventListener
    void onAuditEvent(AuditEvent event) {
        dispatcher.onAuditEvent(event)
    }

}
