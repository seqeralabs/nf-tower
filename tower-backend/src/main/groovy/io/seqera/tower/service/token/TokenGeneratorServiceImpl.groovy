package io.seqera.tower.service.token

import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.security.SecureRandom

@Singleton
@CompileStatic
class TokenGeneratorServiceImpl implements TokenGeneratorService {

    private static final int TOKEN_LENGTH = 20
    private String CHAR_SEQUENCE = "012345679ABCDEFGHIJKLMNOPQRSTUVWXYabcdefghijklmnopqrstuvwxy"

    @Override
    String generateRandomId() {
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");

        StringBuilder stringBuilder = StringBuilder.newInstance();

        1.upto(TOKEN_LENGTH) {
            int randomInt = secureRandom.nextInt(CHAR_SEQUENCE.length());

            stringBuilder.append(CHAR_SEQUENCE.charAt(randomInt));
        }

        return stringBuilder.toString();
    }
}
