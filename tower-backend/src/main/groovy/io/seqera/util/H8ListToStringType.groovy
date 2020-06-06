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
import java.sql.Types

import groovy.transform.CompileStatic
import org.hibernate.engine.spi.SharedSessionContractImplementor
/**
 * Implements a Hibernate custom type mapping a List<String>
 * to a VARCHAR column
 *
 * https://www.baeldung.com/hibernate-custom-types
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class H8ListToStringType extends H8ImmutableType<List> {

    @Override
    int[] sqlTypes() {
        return [Types.CLOB] as int[]
    }


    @Override
    protected List get(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) {
        final list = rs.getString(names[0])
        return list != null ? list.toString().tokenize(',') : null
    }

    @Override
    protected void set(PreparedStatement st, List value, int index, SharedSessionContractImplementor session) {
        final String str = value != null ? value.join(',') : null
        st.setString(index, str)
    }

}
