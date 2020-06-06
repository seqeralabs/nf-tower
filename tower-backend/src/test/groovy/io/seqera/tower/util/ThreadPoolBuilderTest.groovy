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

package io.seqera.tower.util

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class ThreadPoolBuilderTest extends Specification {

    def 'should create thread pool with default' () {

        when:
        def builder = new ThreadPoolBuilder()
                .withMinSize(1)
                .withMaxSize(10)
        then:
        builder.getMinSize() == 1
        builder.getMaxSize() == 10

        when:
        def pool = builder.build()
        then:
        pool.getCorePoolSize() == 1
        pool.getMaximumPoolSize() == 10
        pool.getKeepAliveTime(TimeUnit.MILLISECONDS) == 60_000
        pool.getThreadFactory() instanceof CustomThreadFactory
        pool.getRejectedExecutionHandler() instanceof ThreadPoolExecutor.CallerRunsPolicy
        and:
        builder.getName() == 'tower-0'
        builder.getWorkQueue() instanceof LinkedBlockingQueue
    }

    def 'should create pool with all settings' () {
        when:
        def builder = new ThreadPoolBuilder()
                .withName('foo')
                .withMinSize(1)
                .withMaxSize(10)
                .withKeepAliveTime(100)
                .withQueueSize(1000)
                .withAllowCoreThreadTimeout(true)
                .withRejectionPolicy(new ThreadPoolExecutor.AbortPolicy())
        then:
        builder.name == 'foo'
        builder.getMinSize() == 1
        builder.getMaxSize() == 10
        builder.keepAliveTime == 100
        builder.queueSize == 1000
        builder.allowCoreThreadTimeout
        builder.rejectionPolicy instanceof ThreadPoolExecutor.AbortPolicy

        when:
        def pool = builder.build()
        then:
        pool.getCorePoolSize() == 1
        pool.getMaximumPoolSize() == 10
        pool.getKeepAliveTime(TimeUnit.MILLISECONDS) == 100
        pool.getThreadFactory() instanceof CustomThreadFactory
        pool.getRejectedExecutionHandler() instanceof ThreadPoolExecutor.AbortPolicy
    }
}
