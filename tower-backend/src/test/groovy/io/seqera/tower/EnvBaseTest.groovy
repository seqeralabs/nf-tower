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

import javax.inject.Inject

import io.micronaut.context.ApplicationContext
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.service.cron.CronService
import io.seqera.tower.service.cron.CronServiceImpl
import io.seqera.tower.service.live.LiveEventsService
import io.seqera.tower.service.live.LiveEventsServiceImpl
import io.seqera.tower.service.mail.MailServiceImpl
import io.seqera.tower.service.progress.LocalStatsStore
import io.seqera.tower.service.progress.ProgressStore
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(environments = ['test'])
class EnvBaseTest extends Specification {

    @Inject ApplicationContext ctx

    def 'should expected services' () {
        expect:
        ctx.getBean(CronService) instanceof CronServiceImpl
        ctx.getBean(LiveEventsService) instanceof LiveEventsServiceImpl
        ctx.getBean(MailServiceImpl) instanceof MailServiceImpl
        ctx.getBean(ProgressStore) instanceof LocalStatsStore

    }
}
