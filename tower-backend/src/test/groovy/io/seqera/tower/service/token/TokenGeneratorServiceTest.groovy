package io.seqera.tower.service.token

import io.micronaut.test.annotation.MicronautTest
import io.seqera.tower.Application
import io.seqera.tower.util.AbstractContainerBaseTest

import javax.inject.Inject

@MicronautTest(application = Application.class)
class TokenGeneratorServiceTest extends AbstractContainerBaseTest{

    @Inject
    TokenGeneratorService tokenGeneratorService

    void "a unique token id is generated" () {
        when:
        String token = tokenGeneratorService.generateRandomId();

        then:
        token.size() == 20
    }
}
