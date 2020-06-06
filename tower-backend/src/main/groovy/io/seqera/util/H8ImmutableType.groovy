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

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType

/**
 * Generic Hibernate immutable custom type
 *
 * See
 *  https://vladmihalcea.com/how-to-implement-a-custom-basic-type-using-hibernate-usertype/
 *  https://www.baeldung.com/hibernate-custom-types
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
abstract class H8ImmutableType<T> implements UserType {

    private final Class<T> type

    {
        // infer the generic type class
        this.type = (Class<T>) ((ParameterizedType) this
                .getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0]
    }

    @Override
    Object nullSafeGet( ResultSet rs,
            String[] names,
            SharedSessionContractImplementor session,
            Object owner)
            throws SQLException
    {
        return get(rs, names, session, owner);
    }

    @Override
    void nullSafeSet(
            PreparedStatement st,
            Object value,
            int index,
            SharedSessionContractImplementor session)
            throws SQLException
    {
        set(st, type.cast(value), index, session);
    }

    protected abstract T get(
            ResultSet rs,
            String[] names,
            SharedSessionContractImplementor session,
            Object owner) throws SQLException;

    protected abstract void set(
            PreparedStatement st,
            T value,
            int index,
            SharedSessionContractImplementor session)
            throws SQLException;


    @Override
    Class<T> returnedClass() {
        return type
    }

    @Override
    boolean equals(Object x, Object y) {
        return Objects.equals(x, y);
    }

    @Override
    int hashCode(Object x) {
        return x.hashCode()
    }

    @Override
    Object deepCopy(Object value) {
        return value;
    }

    @Override
    final boolean isMutable() {
        return false
    }

    @Override
    Serializable disassemble(Object o) {
        return (Serializable) o
    }

    @Override
    Object assemble( Serializable cached, Object owner) {
        return cached
    }

    @Override
    Object replace( Object o, Object target, Object owner) {
        return o
    }
}
