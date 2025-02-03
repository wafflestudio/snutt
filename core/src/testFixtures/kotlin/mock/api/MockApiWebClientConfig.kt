package mock.api

import com.wafflestudio.snutt.config.SnuttEvWebClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class MockApiWebClientConfig {
    @Bean
    @Primary
    fun snuttevServer(): SnuttEvWebClient = SnuttEvWebClient(WebClient.create())
}
