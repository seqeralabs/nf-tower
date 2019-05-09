package io.seqera.watchtower.util

import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import spock.lang.Specification

abstract class AbstractContainerBaseSpec extends Specification {

    static final GenericContainer MONGODB_CONTAINER

    static {
//        MONGODB_CONTAINER = new GenericContainer("mongo:4.1")
//                .withExposedPorts(27017)
//                .waitingFor(Wait.forListeningPort())
//        System.setProperty('MONGO_PORT', MONGODB_CONTAINER.getMappedPort(27017).toString())
        MONGODB_CONTAINER = new FixedHostPortGenericContainer("mongo:4.1")
                .withFixedExposedPort(27018, 27017)
                .waitingFor(Wait.forListeningPort())

        MONGODB_CONTAINER.start()

    }


    void cleanup() {
        DomainCreator.cleanupDatabase()
    }
}
