package com.wafflestudio.snu4t.users.service

import com.wafflestudio.snu4t.users.repository.UserRepository
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
class UserNicknameGenerateService(
    private val userRepository: UserRepository,
    @Value("adjectives.txt") adjectivesResource: ClassPathResource,
    @Value("nouns.txt") nounsResource: ClassPathResource,
) {
    private val adjectives = readAndSplitByComma(adjectivesResource)
    private val nouns = readAndSplitByComma(nounsResource)

    private fun readAndSplitByComma(resource: ClassPathResource): List<String> {
        return resource.inputStream
            .readBytes()
            .decodeToString()
            .split("\n")
    }

    suspend fun generateUniqueRandomNickname(): String {
        val nickname = createRandomNickname()
        val tagsWithSameNickname = userRepository.findAllByNicknameStartingWith(nickname)
            .mapNotNull { it.getNicknameTag() }
            .toSet()

        val uniqueTag = createTag(tagsWithSameNickname)
        return "$nickname$TAG_DELIMITER$uniqueTag"
    }

    fun generateRandomNickname(): String {
        val nickname = createRandomNickname()
        val uniqueTag = createTag(emptySet())
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

    companion object {
        private const val TAG_DELIMITER = "#"
    }
}
