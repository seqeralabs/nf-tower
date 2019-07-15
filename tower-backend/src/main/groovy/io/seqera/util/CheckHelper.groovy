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

import java.util.regex.Pattern

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.commons.text.similarity.LevenshteinDistance
/**
 * Validation helper
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class CheckHelper {

    /**
     * Valid a method named parameters map
     *
     * @param name The name of the method, only in the error reported message
     * @param params The actual parameters map
     * @param valid A map providing for each parameter name the valid values
     * @throws IllegalArgumentException when the parameter include an unexpected parameter name or value
     */
    static void checkParams( String name, Map<String,?> params, Map<String,?> valid )  {

        if( !params ) return

        def allKeys = valid.keySet()
        for( String key : params.keySet() ) {
            if( !allKeys.contains(key) )
                throw new IllegalArgumentException("Unknown argument '${key}' for operator '$name' -- Possible arguments: ${allKeys.join(', ')}")

            final value = params.get(key)
            final accepted = valid.get(key)
            if( accepted instanceof Collection ) {
                boolean ok = false
                final itr = accepted.iterator()
                while( !ok && itr.hasNext() )
                    ok |= isValid(value, itr.next())
                if( !ok )
                    throw new IllegalArgumentException("Value '${value}' cannot be used in in parameter '${key}' for operator '$name' -- Possible values: ${(accepted as Collection).join(', ')}")
            }

            else if( !isValid(value, accepted) )
                throw new IllegalArgumentException("Value '${value}' cannot be used in in parameter '${key}' for operator '$name' -- Value don't match: ${accepted}")

        }

    }

    /**
     * Check if the provide value is included in the specified range
     *
     * @param value A value to verify
     * @param range The range it may be a {@link Class} a {@link Collection} of values, a regexp {@link Pattern} or a specific value
     * @return {@code true} is the match is satisfied or {@code false} otherwise
     */
    static boolean isValid( value, range ) {
        if( range instanceof Class && value != null )
            return range.isAssignableFrom(value.class)

        if( range instanceof Collection )
            return range.contains(value)

        if( value != null && range instanceof Pattern )
            return range.matcher(value.toString()).matches()

        value == range
    }

    /**
     *  Verify that all method named parameters are included in the provided list
     *
     * @param name The method name used in the reported error message
     * @param params The list of accepted named parameters
     * @param valid The list of accepted parameter names
     * @throws IllegalArgumentException a parameter is not included the in valid names list
     */
    static void checkParams( String name, Map<String,?> params, List<String> valid )  {
        if( !params ) return

        for( String key : params.keySet() ) {
            if( !valid.contains(key) ) {
                def matches = closest(valid,key) ?: valid
                def message = "Unknown argument '${key}' for operator '$name'. Did you mean one of these?\n" + matches.collect { "  $it"}.join('\n')
                throw new IllegalArgumentException(message)
            }
        }
    }


    /**
     *  Verify that all method named parameters are included in the provided list
     *
     * @param name The method name used in the reported error message
     * @param params The list of accepted named parameters
     * @param valid The list of accepted parameter names
     * @throws IllegalArgumentException a parameter is not included the in valid names list
     */
    static void checkParams( String name, Map<String,?> params, String... valid ) {
        checkParams(name, params, Arrays.asList(valid))
    }


    /**
     * Find all the best matches for the given example string in a list of values
     *
     * @param sample The example string -- cannot be empty
     * @param options A list of string
     * @return The list of options that best matches to the specified example -- return an empty list if none match
     */
    @CompileDynamic
    static List<String> closest(Collection<String> options, String sample ) {
        assert sample

        if( !options )
            return Collections.emptyList()

        // Otherwise look for the most similar
        def diffs = [:]
        options.each {
            diffs[it] = LevenshteinDistance.getDefaultInstance().apply(sample, it)
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
}
