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

import javax.inject.Singleton

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.context.scope.refresh.RefreshEvent
import io.micronaut.runtime.event.annotation.EventListener
/**
 * Object listening for application events
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Singleton
@CompileStatic
class ApplicationListener {

    @EventListener
    void onStartup(StartupEvent event) {
        log.info "Application started up"
    }

    @EventListener
    void onShutdown(ShutdownEvent event) {
        log.info "Application shutting down"
    }

    @EventListener
    void onConfigRefresh(RefreshEvent event) {
        log.info "Got refresh event: " + event.getSource()
    }

}
