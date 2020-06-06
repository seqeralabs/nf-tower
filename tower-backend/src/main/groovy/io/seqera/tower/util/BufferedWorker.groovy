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
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.util.logging.Slf4j
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
class BufferedWorker<T> {

    private final Random rnd = new Random()

    private final Object POISON_PILL = new Object()

    private int attempts

    private int delay

    private Closure onFailure

    private String name = 'debounce'

    private BlockingQueue<T> events = new LinkedBlockingQueue<T>()

    private Long timeoutMillis

    private Integer maxSize

    private Thread thread

    private Closure doOnData

    private Closure doOnComplete

    private boolean shutdownExecutor

    private ExecutorService executor

    {
        thread = Thread.start(this.&main)
    }

    void awaitCompletion() {
        log.debug "< destroy $name"
        events.add(POISON_PILL)
        thread?.join()
        // stop executor
        if( shutdownExecutor ) {
            executor.shutdown()
            executor.awaitTermination(30, TimeUnit.SECONDS)
        }
    }

    protected List<T> newBuffer() {
        maxSize!=null ? new ArrayList(maxSize) : new ArrayList<>()
    }

    protected void main() {
        log.debug "run $name"
        boolean terminated = false
        def buffer = newBuffer()
        while(!terminated) {
            try {
                final el = timeoutMillis ? events.poll(timeoutMillis,TimeUnit.MILLISECONDS) : events.take()
                if( el.is(POISON_PILL) )
                    terminated = true
                else if( el != null ) {
                    buffer.add(el)
                    log.debug "Event received=$el"
                }

            }
            catch(Throwable e) {
                log.debug "Interrupted $name; buffer=$buffer"
                terminated = true
            }

            // check if need to send it out
            boolean trigger = (maxSize!=null && buffer.size()>=maxSize) || terminated
            if( buffer && trigger ) {
                log.debug "Event emitting buffer=$buffer"
                def copy = new ArrayList(buffer)
                buffer.clear()
                if( executor )
                    executor.submit(safeRun(copy, doOnData))
                else
                    safeCall(copy, doOnData)
            }
        }

        try {
            doOnComplete?.call()
        }
        catch (Throwable e) {
            log.error("Unexpected error on debouncer completion", e)
        }
    }

    BufferedWorker withExecutor(ExecutorService executor, boolean shutdown=false) {
        this.executor = executor
        this.shutdownExecutor = shutdown
        return this
    }

    BufferedWorker withBuffer(Integer maxSize, Duration timeout=null) {
        this.timeoutMillis = timeout?.toMillis()
        this.maxSize = maxSize
        return this
    }

    BufferedWorker withBuffer(Integer maxSize, long timeout) {
        this.timeoutMillis = timeout
        this.maxSize = maxSize
        return this
    }

    BufferedWorker withName(String name) {
        this.name = name
        return this
    }

    BufferedWorker onFailure(Closure action ) {
        this.onFailure = action
        return this
    }


    BufferedWorker withAttempts(int n) {
        assert n>0
        this.attempts = n
        return this
    }

    protected Runnable safeRun(List<T> entries, @ClosureParams(value = FromString, options=["List<T>"]) Closure action) {
        new Runnable() {
            @Override
            void run() {
                safeCall(entries, action)
            }
        }
    }
    protected void safeCall(List<T> entries, @ClosureParams(value = FromString, options=["List<T>"]) Closure action) {
        int count=0
        while(true)
        try {
            action.call(entries)
            break
        }
        catch (Exception t1) {
            try {
                if( count++<attempts ) {
                    final d = delay ? 50 + rnd.nextInt(delay) : 0
                    log.error "Unable to handle $name action - attempt $count; sleep $d ms and make another try - cause: ${t1.message ?: t1.cause?.message ?: t1}"
                    if( delay ) sleep(d)
                }
                else {
                    if( onFailure )
                        onFailure.call(t1)
                    else
                        log.error "Unable to handle $name action - Stop retrying after $count attempts", t1
                    break
                }
            }
            catch (Throwable t2) {
                log.error "Oops .. this should not have happened - source exception: $t1", t2
            }
        }
    }


    BufferedWorker onData(@ClosureParams(value = FromString, options=["List<T>"]) Closure action) {
        this.doOnData = action
        return this
    }

    BufferedWorker onComplete(Closure action) {
        this.doOnComplete = action
        return this
    }

    void publish(T event) {
        events.add(event)
    }

}
