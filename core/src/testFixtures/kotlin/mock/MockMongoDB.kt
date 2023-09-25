package mock

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration
class MockMongoDB {
    @Bean
    @ServiceConnection
    fun mongo(): MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:4.0.10")).also { it.start() }
}
