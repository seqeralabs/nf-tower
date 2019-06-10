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
        def result = new BigInteger(1, token).toString(16)
        // pad with extra zeros if necessary
        while( result.size()<40 )
            result = '0'+result
        return result
    }

}
