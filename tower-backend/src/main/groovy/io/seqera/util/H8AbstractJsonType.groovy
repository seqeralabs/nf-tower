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

import java.lang.reflect.ParameterizedType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.hibernate.HibernateException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType

import static io.seqera.util.JacksonHelper.*

/**
 * Implements a Hibernate custom type mapping a {@code T}
 * to a VARCHAR column
 *
 * https://www.baeldung.com/hibernate-custom-types
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
abstract class H8AbstractJsonType<T extends Serializable> implements UserType {


    private final Class<T> type

    {
        // infer the generic type class
        this.type = (Class<T>) ((ParameterizedType) this
                .getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0]
    }

    @Override
    int[] sqlTypes() {
        return [Types.CLOB] as int[]
    }

    @Override
    Class returnedClass() {
        return type
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
        final value = rs.getString(names[0])
        rs.wasNull() ? null : fromJson(value.toString(), type)
    }

    @Override
    void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if( value == null ) {
            st.setNull(index, Types.CLOB)
        }
        else {
            st.setString(index, toJson(value))
        }
    }

    @Override
    @CompileDynamic
    Object deepCopy(Object value) throws HibernateException {
        value
    }

    @Override
    boolean isMutable() {
        return false
    }

    @Override
    Serializable disassemble(Object value) throws HibernateException {
        (Serializable) value
    }

    @Override
    Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached
    }

    @Override
    Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original
    }

}
