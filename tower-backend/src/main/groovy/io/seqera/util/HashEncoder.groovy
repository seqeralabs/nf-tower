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

import groovy.transform.CompileStatic
import org.hashids.Hashids

/**
 * Simple hash encoder/decoder
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class HashEncoder {

    static private Random rnd = new Random()

    static private Hashids HASH_IDS = new Hashids("tower rocks!", 8);

    /**
     * Encode a long to a hash string representation.
     * WARN: value cannot be greater than 9007199254740992L
     *
     * @param value The value to encode
     * @return
     */
    static String encode(long value) {
        HASH_IDS.encode(value)
    }

    static long decodeLong(String str) {
        final ret = HASH_IDS.decode(str)
        if( !ret )
            throw new IllegalArgumentException("Invalid hash id string: $str")
        return ret[0]
    }

}
