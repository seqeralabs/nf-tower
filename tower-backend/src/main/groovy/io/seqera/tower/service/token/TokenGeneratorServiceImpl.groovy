package io.seqera.tower.service.token

import groovy.transform.CompileStatic

import javax.inject.Inject
import javax.inject.Singleton
import java.security.SecureRandom

@Singleton
@CompileStatic
class TokenGeneratorServiceImpl implements TokenGeneratorService {

    private static final int TOKEN_LENGTH = 20
    private String CHAR_SEQUENCE = "012345679ABCDEFGHIJKLMNOPQRSTUVWXYabcdefghijklmnopqrstuvwxy"

    private SecureRandom secureRandom;

    private StringBuilder stringBuilder;

    TokenGeneratorServiceImpl() {
        this.secureRandom = SecureRandom.getInstance("SHA1PRNG")
        this.stringBuilder = StringBuilder.newInstance()
    }

    @Override
    String generateRandomId() {
        1.upto(TOKEN_LENGTH) {
            int randomInt = secureRandom.nextInt(CHAR_SEQUENCE.length());

            stringBuilder.append(CHAR_SEQUENCE.charAt(randomInt));
        }

        String response = stringBuilder.toString();

        stringBuilder.delete(0, TOKEN_LENGTH);

        return response;
    }
}
