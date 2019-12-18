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
import java.sql.SQLException
import java.sql.Types

import groovy.transform.CompileStatic
import org.hibernate.HibernateException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType

/**
 * Implements a Hibernate custom type mapping a List<String>
 * to a VARCHAR column
 *
 * https://www.baeldung.com/hibernate-custom-types
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class H8ListToStringType implements UserType {

    @Override
    int[] sqlTypes() {
        return [Types.CLOB] as int[]
    }

    @Override
    Class returnedClass() {
        return List.class
    }

    @Override
    boolean equals(Object x, Object y) throws HibernateException {
        return x == y
    }

    @Override
    int hashCode(Object x) throws HibernateException {
        return Objects.hashCode(x)
    }

    @Override
    Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        if( rs.wasNull() )
            return null
        assemble(rs.getString(names[0]), owner)
    }

    @Override
    void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if( value == null ) {
            st.setNull(index, Types.VARCHAR);
        }
        else {
            st.setString(index, (String)disassemble(value))
        }
    }

    @Override
    Object deepCopy(Object value) throws HibernateException {
        if( value == null )
            return null
        if( value instanceof List ) {
            return new ArrayList(value)
        }
        else
            throw new IllegalArgumentException("Value is not a List=$value; type=${value.getClass().getName()}")
    }

    @Override
    boolean isMutable() {
        return false
    }

    @Override
    Serializable disassemble(Object value) throws HibernateException {
        if( value == null )
            return null
        if( value instanceof List )
            return value.join(',')
        throw new IllegalArgumentException("Illegal value type: $value; type=${value.getClass().getName()}; expected type=java.util.List")
    }

    @Override
    Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached != null ? cached.toString().tokenize(',') : null
    }

    @Override
    Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original
    }
}
