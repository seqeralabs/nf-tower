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

package io.seqera.tower.service

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

import grails.gorm.services.Query
import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.reactivex.BackpressureStrategy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.seqera.tower.domain.AccessToken
import io.seqera.tower.domain.User
import io.seqera.tower.exceptions.EntityException
import io.seqera.tower.validation.ValidationHelper
import io.seqera.util.TokenHelper
import org.grails.datastore.mapping.validation.ValidationException
import org.springframework.transaction.annotation.Propagation
/**
 * Implements the access token service operations
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@Service(AccessToken)
abstract class AccessTokenService {

    @Value('${tower.access-token.flush.interval:`45s`}')
    Duration flushInterval

    abstract AccessToken getByNameAndUser(String name, User user)

    abstract int countByUser(User user)

    @Query("from $AccessToken as token where token.user = $user")
    abstract List<AccessToken> findByUser(User user)

    @Query("delete ${AccessToken token} where $token.id = $tokenId")
    abstract Integer deleteById(Long tokenId)

    @Query("delete ${AccessToken token} where $token.user = $user")
    abstract Integer deleteByUser(User user)

    @Query("delete ${AccessToken token} where ${token.user}.id = $userId")
    abstract Integer deleteByUserId(Serializable userId)

    AccessToken createToken(String name, User user) {
        def result = new AccessToken()
        result.name = name
        result.user = user
        result.token = TokenHelper.createHexToken()
        result.dateCreated = Instant.now()

        try {
            return result.save(failOnError: true)
        }
        catch (ValidationException e) {
            final err = ValidationHelper.findError(e, ValidationHelper.Code.unique, "name")
            final String msg = (err
                    ? "An access token with name '$name' already exists"
                    : ValidationHelper.formatErrors(e) )
            throw new EntityException(msg)
        }
    }

    @Query("update AccessToken t set t.lastUsed=$ts where t.token = $token")
    abstract Integer updateLastUsed(String token, Instant ts)

    void updateLastUsedAsync(String token) {
        tokenTimestamps.put(token, Instant.now())
        getLastAccessUpdater().onNext(token)
    }

    // token update flushing logic
    private Map<String, Instant> tokenTimestamps = new ConcurrentHashMap<>()

    private PublishSubject lastAccessUpdater

    @Memoized
    PublishSubject getLastAccessUpdater() {
        log.debug "Creating access token publisher interval=$flushInterval"
        lastAccessUpdater = PublishSubject.create()
        lastAccessUpdater
                .toFlowable(BackpressureStrategy.LATEST)
                .observeOn(Schedulers.io())
                .buffer(flushInterval.toMillis(), TimeUnit.MILLISECONDS)
                .subscribe(this.&flushLastAccessUpdates)
        return lastAccessUpdater
    }

    protected void flushLastAccessUpdates(nope) {
        if( !tokenTimestamps )
            return
        try {
            flushLastAccessUpdates0()
        }
        catch (Exception e) {
            log.error "Error while flushing access tokens | ${e.message ?: e.toString()}"
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void flushLastAccessUpdates0() {
        for( Map.Entry<String,Instant> entry : new HashMap<>(tokenTimestamps) ) {
            final token = entry.key
            final ts = entry.value
            log.trace "Flushing access token=${token.substring(0,5)}.. lastUsed=$ts"
            updateLastUsed(token, ts)
            tokenTimestamps.remove(token, ts)
        }
    }

    void stop() {
        flushLastAccessUpdates(null)
    }
}


