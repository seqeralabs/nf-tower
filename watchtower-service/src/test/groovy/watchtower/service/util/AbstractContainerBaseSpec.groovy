package watchtower.service.util

import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import spock.lang.Specification

abstract class AbstractContainerBaseSpec extends Specification {

    static final FixedHostPortGenericContainer MONGODB_CONTAINER

    static {
        MONGODB_CONTAINER = new FixedHostPortGenericContainer("mongo:4.1")
                .withFixedExposedPort(27018, 27017)
                .waitingFor(Wait.forHttp('/'))
        MONGODB_CONTAINER.start()
    }


    void cleanup() {
        DomainCreator.cleanupDatabase()
    }
}
