package com.wafflestudio.snu4t.users.service

import com.wafflestudio.snu4t.users.repository.UserRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toSet
import kotlinx.coroutines.runBlocking
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class UserNicknameGenerateService(
    private val userRepository: UserRepository
) {

    companion object {
        private val adjectives = mutableListOf<String>()
        private val nouns = mutableListOf<String>()
        private const val TAG_DELIMITER = "#"
    }

    @PostConstruct
    fun init() = runBlocking {
        adjectives.addAll(readAndSplitByComma("adjectives.txt"))
        nouns.addAll(readAndSplitByComma("nouns.txt"))
    }

    private fun readAndSplitByComma(filename: String): List<String> {
        val classPathResource = ClassPathResource(filename)
        val dataBufferFlux = DataBufferUtils.read(classPathResource, DefaultDataBufferFactory(), 4096)
        return DataBufferUtils.join(dataBufferFlux)
            .map { buffer ->
                buffer.asInputStream()
                    .readBytes()
                    .decodeToString()
                    .split("\n")
            }
            .block()
            .orEmpty()
    }

    suspend fun generate(userInput: String? = null): String {
        val nickname = userInput.orEmpty().ifEmpty { createRandomNickname() }
        val tagsWithSameNickname = userRepository.findByNicknameStartingWith(nickname)
            .mapNotNull { it.getNicknameTag() }
            .toSet()

        val uniqueTag = createTag(tagsWithSameNickname)
        return "$nickname$TAG_DELIMITER$uniqueTag"
    }

    private fun createRandomNickname(): String {
        val adj = adjectives.random()
        val noun = nouns.random()
        return "$adj $noun"
    }

    private fun createTag(existingTags: Set<Int>): String =
        generateSequence { (0..9999).random() }
            .filter { it !in existingTags }
            .first()
            .toString()
            .padStart(4, '0')
}
