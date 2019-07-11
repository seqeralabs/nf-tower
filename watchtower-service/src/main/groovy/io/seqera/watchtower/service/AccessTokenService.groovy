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

package io.seqera.watchtower.service

import java.time.Instant

import grails.gorm.services.Query
import grails.gorm.services.Service
import io.seqera.util.TokenHelper
import io.seqera.watchtower.domain.AccessToken
import io.seqera.watchtower.domain.User
import io.seqera.watchtower.exceptions.EntityException
import io.seqera.watchtower.validation.ValidationHelper
import org.grails.datastore.mapping.validation.ValidationException
/**
 * Implements the access token service operations
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Service(AccessToken)
abstract class AccessTokenService {

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

    AccessToken createToken(String name, User user ) {
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

}


