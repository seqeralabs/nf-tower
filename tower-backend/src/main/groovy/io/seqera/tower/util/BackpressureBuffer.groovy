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

import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.util.logging.Slf4j
/**
 * Implements back pressure buffers that collect *offered* events
 * to a buffer and emit it within a fixed window of max time or number of items
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class BackpressureBuffer<T> {

    public  final Duration DEFAULT_HEARTBEAT = Duration.parse('PT5M')

    public  final Duration DEFAULT_TIMEOUT = Duration.parse('PT1S')

    public final int DEFAULT_MAX_COUNT = 100

    private LinkedBlockingQueue<T> eventQueue = new LinkedBlockingQueue<>()

    private Thread spooler

    private volatile terminated

    private Duration timeout = DEFAULT_TIMEOUT

    private Duration heartbeat = DEFAULT_HEARTBEAT

    private int maxCount = DEFAULT_MAX_COUNT

    private String name = 'Event buffer spooler'

    private Closure action

    BackpressureBuffer setName(String name) {
        this.name = name
        return this
    }

    BackpressureBuffer setTimeout(Duration timeout) {
        this.timeout = timeout
        return this
    }

    BackpressureBuffer setMaxCount(int count) {
        this.maxCount = count
        return this
    }

    BackpressureBuffer setHeartbeat(Duration duration) {
        this.heartbeat = duration
        return this
    }

    Duration getHeartbeat() { heartbeat }

    Duration getTimeout() { timeout }

    int getMaxCount() { maxCount }

    BackpressureBuffer start () {
        spooler = Thread.startDaemon(name, this.&emitEvents0)
        return this
    }

    void offer(T payload) {
        eventQueue.add(payload)
    }

    BackpressureBuffer onNext( @ClosureParams(value= FromString, options=["List"]) Closure action) {
        this.action = action
        return this
    }

    protected void emitEvents0(dummy) {
        final buffer = new HashMap<Integer,T>(maxCount)
        long previous = System.currentTimeMillis()
        final long period = timeout.toMillis()
        final long pollTimeout = period / 10 as long
        log.trace "Starting backpressure buffer thread | maxSize=$maxCount; timeout=${timeout} (${timeout.toMillis()}ms); heartbeat=$heartbeat (${heartbeat.toMillis()}ms); period=${period}ms; poolTimeout=${pollTimeout}ms"

        while( !terminated ) {
            final event = eventQueue.poll(pollTimeout, TimeUnit.MILLISECONDS)
            // reconcile task events ie. send out only the last event
            if( event ) {
                buffer[event.hashCode()] = event
            }

            // check if there's something to send
            final now = System.currentTimeMillis()
            final delta = now -previous

            if( buffer.size()==0 ) {
                if( delta > heartbeat.toMillis() ) {
                    log.trace "Heartbeat event"
                    action?.call( Collections.emptyList() )
                    previous = now
                }
                continue
            }

            final boolean timeoutFlag = delta > period
            final boolean maxCountFlag = buffer.size() >= maxCount
            if(  timeoutFlag || maxCountFlag || terminated ) {
                log.trace "${ timeout ? 'Timeout' : (maxCountFlag ? 'Max buffer size' : 'Terminate')} event"
                // send
                final List payload = new ArrayList<?>((Collection)buffer.values()).unique()
                action?.call(payload)
                // clean up for next iteration
                previous = now
                buffer.clear()
            }
        }

        if( buffer ) {
            final List payload = new ArrayList<?>((Collection)buffer.values()).unique()
            action?.call(payload)
        }
    }

    void terminate() {
        terminated = true
    }

    void await() {
        spooler?.join()
    }

    void terminateAndAwait() {
        terminate()
        await()
    }
    
}
