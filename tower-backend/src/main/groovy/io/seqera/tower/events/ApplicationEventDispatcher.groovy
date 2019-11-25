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

import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.context.scope.refresh.RefreshEvent
import io.micronaut.security.event.LoginSuccessfulEvent
import io.seqera.tower.service.audit.AuditEvent
/**
 * Decouple application events from the actual listener objects
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
interface ApplicationEventDispatcher {

    void onStartup(StartupEvent event)
    void onShutdown(ShutdownEvent event)
    void onConfigRefresh(RefreshEvent event)
    void onUserLogin(LoginSuccessfulEvent event)
    void onAuditEvent(AuditEvent event)

}
