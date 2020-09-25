package io.seqera.tower.service.token

import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.util.AbstractContainerBaseTest

import javax.inject.Inject

@MicronautTest(application = Application.class)
class TokenGeneratorServiceTest extends AbstractContainerBaseTest {

    @Inject
    TokenGeneratorService tokenGeneratorService

    void "a unique token id is generated with desired length"() {
        when:
        String token = tokenGeneratorService.generateRandomId();

        then:
        token.size() == 20
    }

    void "a set of unique tokens have no collisions"() {
        given:
        def testSequenceLength = 1000000
        def tokens = [:]

        when:
        1.upto(testSequenceLength) {
            def token = tokenGeneratorService.generateRandomId()

            tokens << [(token): token]
        }

        then:
        tokens.size() == testSequenceLength
    }
}
