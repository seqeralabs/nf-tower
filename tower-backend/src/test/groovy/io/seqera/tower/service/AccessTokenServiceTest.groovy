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

import javax.inject.Inject
import java.time.Instant
import java.time.temporal.ChronoUnit

import grails.gorm.transactions.TransactionService
import grails.gorm.transactions.Transactional
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.AccessToken
import io.seqera.tower.domain.User
import io.seqera.tower.exceptions.EntityException
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@MicronautTest(application = Application.class)
@Property(name = "tower.access-token.flush.interval", value = "100ms")
@Transactional
class AccessTokenServiceTest extends AbstractContainerBaseTest {

    @Inject
    AccessTokenService tokenService

    @Inject
    TransactionService tx

    def 'should find user tokens' () {

        given:
        User user = tx.withNewTransaction { new DomainCreator().generateAllowedUser() }

        when:
        def tokens = tokenService.findByUser(user)
        then:
        tokens.size() == 1
        tokens[0].name == 'default'

        when:
        tx.withNewTransaction { tokenService.createToken('new-token', user) }

        and:
        tokens = tx.withNewTransaction { tokenService.findByUser(user) }
        then:
        tokens.size() == 2
        tokens.find { it.name == 'new-token'}.id

    }


    def 'should delete by token id' () {
        given:
        User user = User.withNewTransaction { new DomainCreator().generateAllowedUser() }

        when:
        def tokens = tokenService.findByUser(user)
        then:
        tokens.size() == 1
        
        when:
        def count = tokenService.deleteById(tokens[0].id)
        then:
        count == 1

        when:
        tokens = tokenService.findByUser(user)
        then:
        tokens.size() == 0

    }


    def 'should delete by user' () {
        given:
        User user1 = tx.withNewTransaction { new DomainCreator().createUser(userName:'foo') }
        User user2 = tx.withNewTransaction { new DomainCreator().createUser(userName:'bar') }
        and:
        tx.withTransaction {
            tokenService.createToken('alpha', user1)
            tokenService.createToken('beta', user1)
        }
        
        expect:
        tokenService.countByUser(user1) == 3

        when:
        def deleted = tokenService.deleteByUser(user1)
        then:
        deleted == 3

        and:
        tokenService.findByUser(user1) == []
        tokenService.findByUser(user2).size() == 1

    }

    def 'should delete by user id' () {
        given:
        User user1 = User.withNewTransaction { new DomainCreator().createUser(userName:'foo') }
        User user2 = User.withNewTransaction { new DomainCreator().createUser(userName:'bar') }

        when:
        def deleted = tokenService.deleteByUserId(user2.id)
        then:
        deleted == 1
        and:
        tokenService.findByUser(user1).size() == 1
        tokenService.findByUser(user2).size() == 0
    }


    def 'should get token by name and user' () {

        given:
        User user = tx.withNewTransaction { new DomainCreator().generateAllowedUser() }
        
        expect:
        tokenService.getByNameAndUser('default', user).name == 'default'
        tokenService.getByNameAndUser('unknown', user) == null
    }


    def 'should create a new token' () {
        given:
        User user = tx.withNewTransaction { new DomainCreator().createUser() }
        when:
        tx.withNewTransaction { tokenService.createToken('foo', user) }
        then:
        tokenService.getByNameAndUser('foo', user) != null
    }


    def 'should *not* create a token for existing name' () {
        // creates two users each of which with a `default` token
        given:
        User user1 = tx.withNewTransaction { new DomainCreator().createUser() }
        User user2 = tx.withNewTransaction { new DomainCreator().createUser() }

        // create a new token `foo` for user1
        when:
        tx.withNewTransaction { tokenService.createToken('foo', user1) }
        then:
        tokenService.getByNameAndUser('default', user1) != null
        tokenService.getByNameAndUser('default', user2) != null
        tokenService.getByNameAndUser('foo', user1) != null

        // try to create a new token `foo` for user1
        when:
        tx.withNewTransaction { tokenService.createToken('foo', user1) }
        then:
        // should fail because the name already exists
        thrown(EntityException)
    }

    def 'should create tokens' () {
        given:
        User user1 = tx.withNewTransaction { new DomainCreator().createUser() }
        User user2 = tx.withNewTransaction { new DomainCreator().createUser() }

        expect:
        tokenService.countByUser(user1) ==1
        tokenService.countByUser(user2) ==1
        
        when:
        tx.withNewTransaction {
            tokenService.createToken('foo', user1)
            tokenService.createToken('bar', user1)
        }
        then:
        tx.withNewTransaction {
            tokenService.countByUser(user1) ==3
            tokenService.countByUser(user2) ==1
        }
    }

    def 'should update last used timestamp' () {
        given:
        def creator = new DomainCreator()
        def user = creator.createUser()
        def token = creator.createAccesToken(user:user, name:'foo')
        def ts = Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(10)

        when:
        def result = tx.withNewTransaction { tokenService.updateLastUsed(token.token, ts) }
        then:
        result == 1

        when:
        token = tx.withNewTransaction { AccessToken.get(token.id) }
        then:
        token.lastUsed == ts

    }

    def 'should update timestamp async' () {
        given:
        def creator = new DomainCreator()
        def user = creator.createUser()
        def token = creator.createAccesToken(user:user, name:'foo')
        and:
        def subscriber =  tokenService.lastAccessUpdater.test()

        when:
        tokenService.updateLastUsedAsync(token.token)
        then:
        subscriber.assertValue(token.token)

        when:
        sleep (tokenService.flushInterval.toMillis() +500)
        then:
        tx.withNewTransaction { AccessToken.get(token.id) }.lastUsed != null
    }
}
