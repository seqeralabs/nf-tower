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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
/**
 * Helper class for string utils
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class StringUtils {

    static final public Pattern URL_PROTOCOL = ~/^([a-zA-Z][a-zA-Z0-9]*):\\/\\/(.+)/

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


    /**
     * Find all the best matches for the given example string in a list of values
     *
     * @param sample The example string -- cannot be empty
     * @param options A list of string
     * @return The list of options that best matches to the specified example -- return an empty list if none match
     */
    @CompileDynamic
    static List<String> findSimilar(Collection<String> options, String sample ) {
        assert sample

        if( !options )
            return Collections.emptyList()

        // Otherwise look for the most similar
        def diffs = [:]
        options.each {
            diffs[it] = org.apache.commons.lang3.StringUtils.getLevenshteinDistance(sample, it)
        }

        // sort the Levenshtein Distance and get the fist entry
        def sorted = diffs.sort { it.value }
        def nearest = sorted.find()
        def min = nearest.value
        def len = sample.length()

        def threshold = len<=3 ? 1 : ( len > 10 ? 5 : Math.floor(len/2))

        def result
        if( min <= threshold ) {
            result = sorted.findAll { it.value==min } .collect { it.key }
        }
        else {
            result = []
        }

        return result
    }

    static String getUrlProtocol(String str) {
        final m = URL_PROTOCOL.matcher(str)
        return m.matches() ? m.group(1) : null
    }

    static final private Pattern SHA1_REGEXP = ~/^[0-9a-f]{5,40}$/

    static boolean isSha1String(String str) {
        str ? SHA1_REGEXP.matcher(str).matches() : false
    }

}
