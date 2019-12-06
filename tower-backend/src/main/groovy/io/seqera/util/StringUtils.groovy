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

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Helper class for string utils
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
class StringUtils {

    static private Pattern STAR_REGEX = Pattern.compile("[^*]+|(\\*)")

    static boolean like(String self, String pattern) {
        Pattern.compile( escape(pattern), Pattern.CASE_INSENSITIVE ).matcher(self).matches()
    }

    static protected String escape( String str ){

        Matcher m = STAR_REGEX.matcher(str)
        StringBuffer b= new StringBuffer()
        while (m.find()) {
            if(m.group(1) != null) m.appendReplacement(b, ".*");
            else m.appendReplacement(b, "\\\\Q" + m.group(0) + "\\\\E");
        }
        m.appendTail(b)
        b.toString()
    }

}
