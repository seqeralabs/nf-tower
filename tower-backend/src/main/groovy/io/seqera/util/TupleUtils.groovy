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

/**
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class TupleUtils {

    static List single(obj) {
        final ret = new ArrayList(1)
        ret.add(obj)
        return ret
    }

    static List pair(x, y) {
        final ret = new ArrayList(2)
        ret.add(x)
        ret.add(y)
        return ret
    }

    static List triple(x, y, z) {
        final ret = new ArrayList(3)
        ret.add(x)
        ret.add(y)
        ret.add(z)
        return ret
    }

    static Map map(k1, v1) {
        final result = new HashMap(1)
        result.put(k1,v1)
        return result
    }

    static Map map(k1, v1, k2, v2) {
        final result = new HashMap(2)
        result.put(k1,v1)
        result.put(k2,v2)
        return result
    }

    static Map map(k1, v1, k2, v2, k3, v3) {
        final result = new HashMap(3)
        result.put(k1,v1)
        result.put(k2,v2)
        result.put(k3,v3)
        return result
    }
 }
