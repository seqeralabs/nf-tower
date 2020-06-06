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

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.regex.Pattern

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

/**
 * Represent a memory unit
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@JsonDeserialize
@CompileStatic
@EqualsAndHashCode(includes = 'size', includeFields = true)
class MemUnit implements Comparable<MemUnit>, Serializable, Cloneable {

    final static public MemUnit ZERO = new MemUnit(0)

    final static private Pattern FORMAT = ~/([0-9\.]+)\s*(\S)?B?/

    final static public List UNITS = ["B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB"]

    @JsonValue
    private long size

    static final private DecimalFormatSymbols formatSymbols

    static {
        formatSymbols = new DecimalFormatSymbols()
        formatSymbols.setDecimalSeparator('.' as char)
    }

    /**
     * Default constructor is required by Kryo serializer
     * Do not remove of use directly
     */
    private MemUnit() { this.size = 0 }

    /**
     * Create a memory unit instance
     *
     * @param value The number of bytes it represent
     */
    MemUnit(long value) {
        assert value >= 0, "Memory unit cannot be a negative number"
        this.size = value
    }

    /**
     * Create a memory unit instance with the given semantic string
     *
     * @param str A string using the following of of the following units: B, KB, MB, GB, TB, PB, EB, ZB
     */
    MemUnit(String str) {

        def matcher = FORMAT.matcher(str)
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Not a valid FileSize value: '$str'")
        }

        final value = matcher.group(1)
        final unit = matcher.group(2)?.toUpperCase()

        if (!unit || unit == "B") {
            size = Long.parseLong(value)
        } else {
            int p = UNITS.indexOf(unit)
            if (p == -1) {
                // try adding a 'B' specified
                p = UNITS.indexOf(unit + 'B')
                if (p == -1) {
                    throw new IllegalArgumentException("Not a valid file size unit: ${str}")
                }
            }

            size = Math.round(Double.parseDouble(value) * Math.pow(1024, p))
        }

    }

    long getBytes() { size }

    long getKilo() { size >> 10 }

    long getMega() { size >> 20 }

    long getGiga() { size >> 30 }

    MemUnit plus(MemUnit value) {
        return value != null ? new MemUnit(size + value.size) : this
    }

    MemUnit minus(MemUnit value) {
        return value != null ? new MemUnit(size - value.size) : this
    }

    MemUnit multiply(Number value) {
        return new MemUnit((long) (size * value))
    }

    MemUnit div(Number value) {
        return new MemUnit(Math.round((double) (size / value)))
    }

    String toString() {
        if (size <= 0) {
            return "0"
        }

        // see http://stackoverflow.com/questions/2510434/format-bytes-to-kilobytes-megabytes-gigabytes
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024))
        final formatter = new DecimalFormat("0.#", formatSymbols)
        formatter.setGroupingUsed(false)
        formatter.format(size / Math.pow(1024, digitGroups)) + " " + UNITS[digitGroups]
    }

    @Override
    int compareTo(MemUnit that) {
        return this.size <=> that.size
    }

    static int compareTo(MemUnit left, Object right) {
        assert left

        if (right == null)
            throw new IllegalArgumentException("Not a valid memory value: null")

        if (right instanceof MemUnit)
            return left <=> (MemUnit) right

        if (right instanceof Number)
            return left.size <=> right.toLong()

        if (right instanceof CharSequence)
            return left <=> MemUnit.of(right.toString())

        throw new IllegalArgumentException("Not a valid memory value: $right")
    }

    static MemUnit of(String value) {
        new MemUnit(value)
    }

    static MemUnit of(long value) {
        new MemUnit(value)
    }

    boolean asBoolean() {
        return size != 0
    }

    /**
     * Function to parse/convert given memory unit
     *
     * @param unit String expressing memory unit in bytes, e.g. KB, MB, GB
     */
    long toUnit(String unit) {
        int p = UNITS.indexOf(unit)
        if (p == -1)
            throw new IllegalArgumentException("Not a valid memory unit: $unit")
        return size.intdiv(Math.round(Math.pow(1024, p)))
    }
}
