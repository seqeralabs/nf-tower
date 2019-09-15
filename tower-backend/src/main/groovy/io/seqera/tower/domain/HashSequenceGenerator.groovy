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

package io.seqera.tower.domain


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.hashids.Hashids
import org.hibernate.HibernateException
import org.hibernate.MappingException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.enhanced.SequenceStyleGenerator
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.LongType
import org.hibernate.type.Type
/**
 * Custom sequence generator creating a hash-based unique key
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class HashSequenceGenerator extends SequenceStyleGenerator {

    public final static String SEQUENCE_NAME = 'tw_sequence'

    static private Hashids hashIds = new Hashids("tower rocks!", 8);

    static String getHash(Long id) {
        hashIds.encode(id)
    }

    @Override
    Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        final num = (Long)super.generate(session, object)
        final hash = hashIds.encode(num)
        return hash
    }

    @Override
     void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        params.put('sequence_name', SEQUENCE_NAME)
        super.configure(LongType.INSTANCE, params, serviceRegistry);
    }
}
