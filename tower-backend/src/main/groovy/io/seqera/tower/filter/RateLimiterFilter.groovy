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

package io.seqera.tower.filter

import javax.inject.Named
import java.util.concurrent.TimeUnit

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.internal.AtomicRateLimiter
import io.github.resilience4j.ratelimiter.internal.AtomicRateLimiter.AtomicRateLimiterMetrics
import io.micronaut.cache.SyncCache
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.FilterOrderProvider
import io.micronaut.http.filter.OncePerRequestHttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.reactivex.Flowable
import io.seqera.tower.exchange.MessageResponse
import org.reactivestreams.Publisher
/**
 * Implements an API rate limiter
 *
 * Enable it adding in the `application.yml` the following snippet
 *
 * rate-limiter:
 *   timeout-duration: 100ms
 *   limit-refresh-period: 1s
 *   limit-for-period: 3
 *
 * https://blog.98elements.com/http-request-rate-limiting-with-micronaut/
 * https://medium.com/@storozhuk.b.m/rate-limiter-internals-in-resilience4j-48776e433b90
 * https://www.baeldung.com/resilience4j
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
@ConfigurationProperties("rate-limiter")
@Filter("/**")
class RateLimiterFilter extends OncePerRequestHttpServerFilter implements FilterOrderProvider {

    private final RateLimiterConfig config

    private final SyncCache<AtomicRateLimiter> limiters

    /**
     * Creates the rate limiter with the provided config
     *
     * @param limiters
     * @param opts
     */
    RateLimiterFilter(@Named("rate-limiter") SyncCache<AtomicRateLimiter> limiters, RateLimiterOptions opts) {
        if( !opts.isDisabled() ) {
            log.debug "API rate-limiter filter: limitRefreshPeriod=$opts.limitRefreshPeriod; limitForPeriod=$opts.limitForPeriod; timeoutDuration=$opts.timeoutDuration"
            opts.validate()

            this.limiters = limiters
            this.config = RateLimiterConfig.custom()
                    .limitRefreshPeriod(opts.limitRefreshPeriod)
                    .limitForPeriod(opts.limitForPeriod)
                    .timeoutDuration(opts.timeoutDuration)
                    .build()
        }
        else {
            log.debug "API rate-limiter filter DISABLED"
        }

    }

    @Override
    int getOrder() {
        return LOWEST_PRECEDENCE
    }

    @Override
    protected Publisher<MutableHttpResponse<?>> doFilterOnce(HttpRequest<?> request, ServerFilterChain chain) {
        return limiters!=null ? limit0(request,chain) : chain.proceed(request)
    }

    private Publisher<MutableHttpResponse<?>> limit0(HttpRequest<?> request, ServerFilterChain chain) {
        final key = getKey(request);
        final limiter = getLimiter(key);

        if (limiter.acquirePermission()) {
            log.trace "Rate OK for key: $key"
            return chain.proceed(request)
        }
        else {
            final metrics = limiter.getDetailedMetrics()
            log.trace "Too many request for key: $key -- Wait ${TimeUnit.NANOSECONDS.toMillis(metrics.getNanosToWait())} millis issuing a new request"
            return createOverLimitResponse(metrics);
        }
    }

    private String getKey(HttpRequest<?> request) {
        final  opt = request.getUserPrincipal()
        opt.isPresent() ? opt.get() : request.getRemoteAddress().getAddress().getHostAddress()
    }

    private AtomicRateLimiter getLimiter(String key) {
        return limiters.get(key, AtomicRateLimiter, { new AtomicRateLimiter(key, config) } );
    }

    private Publisher<MutableHttpResponse<?>> createOverLimitResponse(AtomicRateLimiterMetrics metrics) {
        final secondsToWait = TimeUnit.NANOSECONDS.toSeconds(metrics.getNanosToWait())

        final message = "Maximum request rate exceeded. Wait " + secondsToWait + "secs before issuing a new request";
        final body = new MessageResponse(message)
        final resp = HttpResponse
                        .status(HttpStatus.TOO_MANY_REQUESTS)
                        .header(HttpHeaders.RETRY_AFTER, String.valueOf(secondsToWait))
                        .body(body)
        return Flowable.just(resp)
    }

}
