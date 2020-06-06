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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
/**
 * Helper class for rx management
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class RxHelper {
    /**
     * Helper method to sync with the completion of an ob
     * @param observable
     */
    static void await(Observable observable, Duration timeout=null) {
        final latch = new CountDownLatch(1)
        observable.subscribeWith( new Observer() {

            @Override
            void onComplete() {
                latch.countDown()
            }

            @Override
            void onError(@NonNull Throwable e) {
                latch.countDown()
            }

            @Override
            void onSubscribe(@NonNull Disposable d) { }

            @Override
            void onNext(@NonNull Object o) { }

        })

        // await completion
        timeout!=null ? latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS) : latch.await()
    }


}
