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

import java.time.temporal.Temporal

import com.fasterxml.jackson.annotation.JsonValue
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.time.DurationFormatUtils

/**
 * Represent a time unit
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
@EqualsAndHashCode(includes = 'duration')
class TimeUnit implements Comparable<TimeUnit>, Serializable, Cloneable {

    static private final FORMAT = ~/^(\d+\.?\d*)\s*([a-zA-Z]+)/

    static private final LEGACY = ~/^(\d{1,2}):(\d{1,2}):(\d{1,2})$/

    static private final List<String> MILLIS = ['ms','milli','millis']

    static private final List<String> SECONDS = ['s','sec','second','seconds']

    static private final List<String> MINUTES = ['m','min','minute','minutes']

    static private final List<String> HOURS = ['h','hour','hours']

    static private final List<String> DAYS = ['d','day','days']

    static public final List<String> UNITS

    static {
        UNITS = []
        UNITS.addAll(MILLIS)
        UNITS.addAll(SECONDS)
        UNITS.addAll(MINUTES)
        UNITS.addAll(HOURS)
        UNITS.addAll(DAYS)
    }

    /**
     * TimeUnit in millis
     */
    @JsonValue
    long duration

    /**
     * Create e a duration object having the specified number of millis
     *
     * @param duration The duration as milliseconds
     */
    TimeUnit(long duration) {
        assert duration>=0, "TimeUnit unit cannot be a negative number"
        this.duration = duration
    }


    /**
     * Default constructor is required by Kryo serializer
     * Do not removed or use it directly
     */
    private TimeUnit() { duration=0 }

    /**
     * Create the object using a string 'duration' format.
     * Accepted prefix are:
     * <li>{@code ms}, {@code milli}, {@code millis}: for milliseconds
     * <li>{@code s}, {@code second}, {@code seconds}: for seconds
     * <li>{@code m}, {@code minute}, {@code minutes}: for minutes
     * <li>{@code h}, {@code hour}, {@code hours}: for hours
     * <li>{@code d}, {@code day}, {@code days}: for days
     *
     *
     * @param str
     */
    TimeUnit(String str) {

        try {
            try {
                duration = parseSimple(str)
            }
            catch( IllegalArgumentException e ) {
                duration = parseLegacy(str)
            }
        }
        catch( IllegalArgumentException e ) {
            throw e
        }
        catch( Exception e ) {
            throw new IllegalArgumentException("Not a valid duration value: ${str}", e)
        }
    }

    /**
     * Parse a duration string in legacy format i.e. hh:mm:ss
     *
     * @param str The string to be parsed e.g. {@code 05:10:30} (5 hours, 10 min, 30 seconds)
     * @return The duration in millisecond
     */
    private long parseLegacy( String str ) {
        def matcher = (str =~ LEGACY)
        if( !matcher.matches() )
            new IllegalArgumentException("Not a valid duration value: ${str}")

        def groups = (List<String>)matcher[0]
        def hh = groups[1].toInteger()
        def mm = groups[2].toInteger()
        def ss = groups[3].toInteger()

        return java.util.concurrent.TimeUnit.HOURS.toMillis(hh) + java.util.concurrent.TimeUnit.MINUTES.toMillis(mm) + java.util.concurrent.TimeUnit.SECONDS.toMillis(ss)
    }

    /**
     * Parse a duration string
     *
     * @param str A duration string containing one or more component e.g. {@code 1d 3h 10mins}
     * @return  The duration in millisecond
     */
    private long parseSimple( String str ) {

        long result=0
        for( int i=0; true; i++ ) {
            def matcher = (str =~ FORMAT)
            if( matcher.find() ) {
                def groups = (List<String>)matcher[0]
                def all = groups[0]
                def digit = groups[1]
                def unit = groups[2]

                result += convert( digit.toFloat(), unit )
                str = str.substring(all.length()).trim()
                continue
            }

            if( i == 0 || str )
                throw new IllegalArgumentException("Not a valid duration value: ${str}")
            break
        }

        return result
    }

    /**
     * Parse a single duration component
     *
     * @param digit
     * @param unit A valid duration unit e.g. {@code d}, {@code d}, {@code h}, {@code hour}, etc
     * @return The duration in millisecond
     */
    private long convert( float digit, String unit ) {

        if( unit in MILLIS ) {
            return Math.round(digit)
        }
        if ( unit in SECONDS ) {
            return Math.round(digit * 1_000)
        }
        if ( unit in MINUTES ) {
            return Math.round(digit * 60 * 1_000)
        }
        if ( unit in HOURS ) {
            return Math.round(digit * 60 * 60 * 1_000)
        }
        if ( unit in DAYS ) {
            return Math.round(digit * 24 * 60 * 60 * 1_000)
        }

        throw new IllegalStateException()
    }

    TimeUnit(long value, java.util.concurrent.TimeUnit unit) {
        assert value>=0, "TimeUnit unit cannot be a negative number"
        assert unit, "Time unit cannot be null"
        this.duration = unit.toMillis(value)
    }

    static TimeUnit of(long value ) {
        new TimeUnit(value)
    }

    static TimeUnit of(String str ) {
        new TimeUnit(str)
    }

    static TimeUnit of(String str, TimeUnit fallback ) {
        try {
            return new TimeUnit(str)
        }
        catch( IllegalArgumentException e ) {
            log.debug "Not a valid duration value: $str -- Fallback on default value: $fallback"
            return fallback
        }
    }

    static TimeUnit between(Temporal start, Temporal end ) {
        new TimeUnit(java.time.Duration.between(start, end).toMillis())
    }

    long toMillis() {
        duration
    }

    long getMillis() {
        duration
    }

    long toSeconds() {
        java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(duration)
    }

    long getSeconds() {
        toSeconds()
    }

    long toMinutes() {
        java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(duration)
    }

    long getMinutes() {
        toMinutes()
    }

    long toHours() {
        java.util.concurrent.TimeUnit.MILLISECONDS.toHours(duration)
    }

    long getHours() {
        toHours()
    }

    long toDays() {
        java.util.concurrent.TimeUnit.MILLISECONDS.toDays(duration)
    }

    long getDays() {
        toDays()
    }

    /**
     * TimeUnit formatting utilities and constants. The following table describes the tokens used in the pattern language for formatting.
     * <p>
     * <pre>
     *   character	duration element
     *   y	        years
     *   d	        days
     *   H	        hours
     *   m	        minutes
     *   s	        seconds
     * </pre>
     *
     * @param fmt
     * @return
     */
    String format( String fmt ) {
        DurationFormatUtils.formatDuration(duration, fmt)
    }

    String toString() {

        // just prints the milliseconds
        if( duration < 1_000 ) {
            return duration + 'ms'
        }

        // when less than 60 seconds round up to 100th of millis
        if( duration < 60_000 ) {
            return String.valueOf( Math.round(duration / 1_000 * 10 as float) / 10 ) + 's'
        }

        def secs
        def mins
        def hours
        def days
        def result = []

        // round up to seconds
        secs = Math.round( (double)(duration / 1_000) )

        mins = secs.intdiv(60)
        secs = secs % 60
        if( secs )
            result.add( secs+'s' )

        hours = mins.intdiv(60)
        mins = mins % 60
        if( mins )
            result.add(0, mins+'m' )

        days = hours.intdiv(24)
        hours = hours % 24
        if( hours )
            result.add(0, hours+'h' )

        if( days )
            result.add(0, days+'d')

        return result.join(' ')
    }

    def plus(TimeUnit value )  {
        return new TimeUnit( duration + value.duration )
    }

    def minus(TimeUnit value )  {
        return new TimeUnit( duration - value.duration )
    }

    def multiply( Number value ) {
        return new TimeUnit( (long)(duration * value) )
    }

    def div( Number value ) {
        return new TimeUnit( Math.round((double)(duration / value)) )
    }

    boolean asBoolean() {
        return duration != 0
    }

    @Override
    int compareTo(TimeUnit that) {
        return this.duration <=> that.duration
    }

    static int compareTo(TimeUnit left, Object right) {
        assert left

        if( right==null )
            throw new IllegalArgumentException("Not a valid duration value: null")

        if( right instanceof TimeUnit )
            return left <=> (TimeUnit)right

        if( right instanceof Number )
            return left.duration <=> right.toLong()

        if( right instanceof CharSequence )
            return left <=> TimeUnit.of(right.toString())

        throw new IllegalArgumentException("Not a valid duration value: $right")
    }

}
