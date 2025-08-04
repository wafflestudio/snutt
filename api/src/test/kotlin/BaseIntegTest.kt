import io.kotest.core.spec.style.WordSpec
import mock.MockMongoDB
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(MockMongoDB::class)
abstract class BaseIntegTest(
    body: BaseIntegTest.() -> Unit = {},
) : WordSpec() {
    init {
        body()
    }
}
