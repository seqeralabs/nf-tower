package io.seqera.tower.service.token

import groovy.transform.CompileStatic
import org.apache.commons.lang3.RandomStringUtils

import javax.inject.Singleton

@Singleton
@CompileStatic
class TokenGeneratorServiceImpl implements TokenGeneratorService {

    private static final int TOKEN_RANDOM_AGGREGATOR_LENGTH = 7;

    @Override
    String generateRandomId() {
        Date date = new Date();

        return RandomStringUtils.randomNumeric(TOKEN_RANDOM_AGGREGATOR_LENGTH) + date.getTime()
    }
}
