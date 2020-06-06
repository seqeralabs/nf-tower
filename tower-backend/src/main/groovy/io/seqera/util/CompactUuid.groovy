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

import com.devskiller.friendly_id.FriendlyId
import groovy.transform.CompileStatic

/**
 * Encode a UUID to Base62 to make shorter and URL friendly
 *
 * See https://github.com/Devskiller/friendly-id
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class CompactUuid {

    /**
     * Generate a random UUID with short encoding
     *
     * @return The UUID encoded as string up to 22 characters
     */
    static String generate() {
        return FriendlyId.toFriendlyId(UUID.randomUUID())
    }

    /**
     * @param uuid The {@link UUID} identifier from which generated the unique string
     * @return The UUID encoded as string up to 22 characters
     */
    static String encode(UUID uuid) {
        return FriendlyId.toFriendlyId(uuid)
    }

    static String encode(String uuid) {
        final key = UUID.fromString(uuid)
        return FriendlyId.toFriendlyId(key)
    }

    static UUID decodeToUuid(String key) {
        return FriendlyId.toUuid(key)
    }

    static byte[] fromStringToBytes(String key) {
        final uuid = FriendlyId.toUuid(key)
        uuidToBytes(uuid)
    }

    static String fromBytesToString(byte[] bytes) {
        final uuid = bytesToUuid(bytes)
        FriendlyId.toFriendlyId(uuid)
    }

    static byte[] decodeToBytes(String key) {
        final uuid = FriendlyId.toUuid(key)
        return uuidToBytes(uuid)
    }

    protected static byte[] uuidToBytes(UUID uuid) {
        byte[] result = new byte[16]
        longToBytes0(uuid.mostSignificantBits, result, 0)
        longToBytes0(uuid.leastSignificantBits, result, 8)
        return result
    }

    protected static UUID bytesToUuid(byte[] bytes) {
        final msb = bytesToLong0(bytes,0)
        final lsb = bytesToLong0(bytes, 8)
        return new UUID(msb, lsb)
    }

    protected static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        longToBytes0(l,result,0)
        return result;
    }

    protected static void longToBytes0(long l, byte[] result, int offset) {
        for (int i = 7; i >= 0; i--) {
            result[i+offset] = (byte)(l & 0xFF)
            l >>= 8;
        }
    }

    protected static long bytesToLong0(final byte[] bytes, final int offset) {
        long result = 0;
        for (int i = offset; i < Long.BYTES + offset; i++) {
            result <<= Long.BYTES;
            result |= (bytes[i] & 0xFF);
        }
        return result;
    }

    protected static long bytesToLong(byte[] bytes) {
        bytesToLong0(bytes,0)
    }

}
