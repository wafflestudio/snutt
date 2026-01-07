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
    fun mockMongo(): MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:8.2.1")).also { it.start() }
}
