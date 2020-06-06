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

package io.seqera.util

import java.sql.PreparedStatement
import java.sql.ResultSet

import io.seqera.tower.domain.UserOptions
import io.seqera.util.H8UserOptionsType
import org.hibernate.engine.spi.SharedSessionContractImplementor
import spock.lang.Specification
/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class H8UserOptionsTypeTest extends Specification {

    def 'should ser de-ser options' () {
        given:
        def h8 = new H8UserOptionsType()

        def json = null
        def stm = Mock(PreparedStatement) {
            setString(_,_) >> { args -> json = args[1] }
        }

        when:
        def opts = new UserOptions(githubToken: 'xyz')
        h8.nullSafeSet(stm, opts, 0, Mock(SharedSessionContractImplementor))
        then:
        json == '{"githubToken":"xyz"}'

    }

    def 'should de-ser options' () {
        given:
        def h8 = new H8UserOptionsType()
        def JSON = '{"githubToken":"xyz"}'
        def rs = Mock(ResultSet)

        when:
        UserOptions opts = h8.nullSafeGet(rs, ['options'] as String[], Mock(SharedSessionContractImplementor), Mock(UserOptions))
        then:
        1 * rs.getString(_) >> JSON
        1 * rs.wasNull() >> false
        opts.githubToken == 'xyz'

        when:
        def empty = h8.nullSafeGet(rs, ['options'] as String[], Mock(SharedSessionContractImplementor), Mock(UserOptions))
        then:
        1 * rs.getString(_) >> null
        1 * rs.wasNull() >> true
        and:
        empty == null

    }
}
