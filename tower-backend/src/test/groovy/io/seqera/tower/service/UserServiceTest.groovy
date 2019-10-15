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
import javax.validation.ValidationException
import java.time.OffsetDateTime

import grails.gorm.transactions.TransactionService
import grails.gorm.transactions.Transactional
import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.domain.User
import io.seqera.tower.domain.UserRole
import io.seqera.tower.exceptions.NonExistingUserException
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator
import io.seqera.util.StringUtils
import org.grails.datastore.mapping.validation.ValidationException as GrailsValidationException
import spock.lang.IgnoreIf

@MicronautTest(application = Application.class, environments = ['test','trusted'])
@Transactional
class UserServiceTest extends AbstractContainerBaseTest {

    @Inject
    UserService userService

    @Inject
    AccessTokenService accessTokenService

    @Inject
    TransactionService tx

    def 'should create a user' () {
        given:
        def EMAIL = 'random@email.com'

        when:
        User user = userService.create(EMAIL,'ROLE_USER')
        then:
        user.email == EMAIL 
        user.userName == 'random'
        user.id != null
        and:
        !user.trusted
        !user.trusted
        !user.authToken
        user.accessTokens.size() == 1

        and:
        User.get(user.id) == user
        UserRole.findByUser(user).role.authority == 'ROLE_USER'
    }

    def 'should create a trusted user' () {
        given:
        def EMAIL = 'me@hack.com'
        and:
        // there should be a `*@hack.com` in the application-test.yml
        assert (userService as UserServiceImpl).trustedEmails.find { StringUtils.like(EMAIL, it) }

        when:
        User user = userService.create(EMAIL,'ROLE_USER')
        then:
        user.email == EMAIL
        user.userName == 'me'
        user.id != null
        and:
        user.trusted

        and:
        User.get(user.id) == user
        UserRole.findByUser(user).role.authority == 'ROLE_USER'
    }



    void "update an existing user given new user data"() {
        given: 'an existing user'
        User user = new DomainCreator().createUser()

        and: 'some new data encapsulated in a user object'
        User userData = new DomainCreator(save: false).createUser(userName: 'user', firstName: 'User', lastName: 'Userson', avatar: 'https://i.pravatar.cc/200', organization: 'Org', description: 'Desc')

        when: 'update the user'
        User updatedUser
        User.withNewTransaction {
            updatedUser = userService.update(user, userData)
        }

        then: "the user has been correctly updated"
        updatedUser.userName == userData.userName
        updatedUser.firstName == userData.firstName
        updatedUser.lastName == userData.lastName
        updatedUser.avatar == userData.avatar
        updatedUser.organization == userData.organization
        updatedUser.description == userData.description
        User.withNewTransaction {
            User.count() == 1
        }
    }

    void "try to update an existing user, but with some invalid data"() {
        given: 'an existing user'
        User user = new DomainCreator().createUser()

        and: 'some new data encapsulated in a user object'
        User userData = new DomainCreator(save: false).createUser(avatar: 'badUrl')

        when: 'update the user'
        User.withNewTransaction {
            userService.update(user, userData)
        }

        then: "a validation exception is thrown"
        ValidationException e = thrown(ValidationException)
        e.message == "Can't save a user with bad avatar URL format"
    }

    void "try to update a non existing user"() {
        given: 'some new data encapsulated in a user object'
        User userData = new DomainCreator(save: false).createUser(avatar: 'badUrl')

        when: 'update a non existing user'
        User.withNewTransaction {
            userService.update(null, userData)
        }

        then: "a non-existing exception is thrown"
        NonExistingUserException e = thrown(NonExistingUserException)
        e.message == "The user to update doesn't exist"
    }

    void "delete an existing user without roles"() {
        given: 'an existing user'
        User user = new DomainCreator().createUser()

        when: 'remove the user'
        User.withNewTransaction {
            userService.delete(user)
        }

        then: "the user has been correctly deleted"
        User.withNewTransaction {
            User.count() == 0
        }
    }

    void "delete an existing user with roles and associated workflows"() {
        given: 'an existing user'
        User user = new DomainCreator().createUser()

        and: "grant a role to the user"
        new DomainCreator().createUserRole(user: user)

        and: 'associate some workflows to the user'
        3.times {
            new DomainCreator().createWorkflow(owner: user)
        }

        when: 'remove the user'
        User.withNewTransaction {
            userService.delete(user)
        }

        then: "the user has been correctly deleted"
        User.withNewTransaction {
            User.count() == 0
        }
    }

    void "try to delete an nonexistent user"() {
        when: 'remove a non existing user'
        User.withNewTransaction {
            userService.delete(null)
        }

        then: "a non-existing exception is thrown"
        NonExistingUserException e = thrown(NonExistingUserException)
        e.message == "The user to delete doesn't exist"
    }


    def 'should find a user by the access token' () {
        given:
        User user = tx.withNewTransaction { new DomainCreator().createUser() }
        def tokens = accessTokenService.findByUser(user)

        expect:
        tokens.size()==1

        when:
        def expected = userService.getByAccessToken(tokens.get(0).token)
        then:
        user.id == expected.id
    }

    def 'user name should not start with number' () {
        when: 'starts with a number'
        tx.withNewTransaction { new DomainCreator().createUser(userName: '0abc') }
        then:
        notThrown(GrailsValidationException)

        when: 'contains more than one -'
        tx.withNewTransaction { new DomainCreator().createUser(userName: 'a----b') }
        then:
        thrown(GrailsValidationException)

        when: 'contains blanks'
        tx.withNewTransaction { new DomainCreator().createUser(userName: 'a b') }
        then:
        thrown(GrailsValidationException)

        when: 'uppercase is used'
        tx.withNewTransaction { new DomainCreator().createUser(userName: 'ABC') }
        then:
        thrown(GrailsValidationException)

        when: 'underscore is used'
        tx.withNewTransaction { new DomainCreator().createUser(userName: 'a_b') }
        then:
        thrown(GrailsValidationException)

        when: 'starts with -'
        tx.withNewTransaction { new DomainCreator().createUser(userName: '-aa') }
        then:
        thrown(GrailsValidationException)

        when: 'ends with -'
        tx.withNewTransaction { new DomainCreator().createUser(userName: 'aa-') }
        then:
        thrown(GrailsValidationException)
    }


    @IgnoreIf({ System.getenv('CODEBUILD_BUILD_ID') })
    def 'should fetch avatar image url from the email' () {

        given:
        def svc = userService as UserServiceImpl

        expect:
        svc.getAvatarUrl(EMAIL) == AVATAR

        where:
        EMAIL                       | AVATAR
        'paolo.ditommaso@gmail.com' | 'https://www.gravatar.com/avatar/21c5e4164ca1573516b6a378fc279df2?d=404'
        'unknown@foo.com'           | null

    }

    def 'should update last access ts' () {
        given:
        def ts = OffsetDateTime.now()
        def creator = new DomainCreator()

        when:
        def user = creator.createUser()
        then:
        user.lastAccess == null

        when:
        def done = userService.updateLastAccessTime(user.id)
        then:
        done 
        User.get(user.id).lastAccess >= ts.minusSeconds(2)
        User.get(user.id).lastAccess <= ts.plusSeconds(2)
    }

}
