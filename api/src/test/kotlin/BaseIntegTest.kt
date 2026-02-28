import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.snutt.common.mail.MailClient
import com.wafflestudio.snutt.common.storage.StorageClient
import io.kotest.core.spec.style.WordSpec
import mock.MockMongoDB
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(MockMongoDB::class)
@MockkBean(types = [StorageClient::class, MailClient::class], relaxed = true)
abstract class BaseIntegTest(
    body: BaseIntegTest.() -> Unit = {},
) : WordSpec() {
    init {
        body()
    }
}
