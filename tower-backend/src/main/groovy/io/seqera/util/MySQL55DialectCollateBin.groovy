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
import org.hibernate.dialect.MySQL55Dialect

/**
 * Custom MySQL dialect that enables UTF-8 character encoding
 * and binary collation.
 * https://dev.mysql.com/doc/refman/5.7/en/charset-binary-collations.html
 *
 * TLDR; in a nutshell by default varchar primary key and index
 * are case-insensitive (!). Use binary collation to make them
 * case sensitive.
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class MySQL55DialectCollateBin extends MySQL55Dialect {

    @Override
    String getTableTypeString() {
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin"
    }
}
