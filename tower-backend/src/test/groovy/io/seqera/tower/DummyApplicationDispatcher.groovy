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

package io.seqera.tower

import javax.inject.Singleton

import io.micronaut.context.annotation.Replaces
import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.context.scope.refresh.RefreshEvent
import io.micronaut.security.event.LoginSuccessfulEvent
import io.seqera.tower.service.audit.AuditEvent
import io.seqera.tower.events.ApplicationEventDispatcher
import io.seqera.tower.events.ApplicationEventDispatcherImpl
/**
 * Dummy dispatcher to disable application events during tests
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Singleton
@Replaces(ApplicationEventDispatcherImpl)
class DummyApplicationDispatcher implements ApplicationEventDispatcher {

    @Override
    void onStartup(StartupEvent event) { }

    @Override
    void onShutdown(ShutdownEvent event) { }

    @Override
    void onConfigRefresh(RefreshEvent event) { }

    @Override
    void onUserLogin(LoginSuccessfulEvent event) { }

    @Override
    void onAuditEvent(AuditEvent event) {

    }
}
