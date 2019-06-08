package io.seqera.util

import java.security.SecureRandom

import groovy.transform.CompileStatic

/**
 * Helper class to create secure random tokens
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class TokenHelper {

    /**
     * Create secure cryptographic random tokens
     *
     * See https://stackoverflow.com/a/44227131/395921
     *
     * @return A 40 hex-characters random string 
     */
    static String createHexToken() {
        final secureRandom = new SecureRandom();
        byte[] token = new byte[20]
        secureRandom.nextBytes(token)
        new BigInteger(1, token).toString(16)
    }

}
