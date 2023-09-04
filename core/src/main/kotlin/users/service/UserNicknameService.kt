package com.wafflestudio.snu4t.users.service

import com.wafflestudio.snu4t.common.exception.InvalidNicknameException
import com.wafflestudio.snu4t.users.dto.NicknameDto
import com.wafflestudio.snu4t.users.repository.UserRepository
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
class UserNicknameService(
    private val userRepository: UserRepository,
    @Value("adjectives.txt") adjectivesResource: ClassPathResource,
    @Value("nouns.txt") nounsResource: ClassPathResource,
) {
    private val adjectives = readAndSplitByComma(adjectivesResource)
    private val nouns = readAndSplitByComma(nounsResource)

    companion object {
        private const val TAG_DELIMITER = "#"
        private const val NICKNAME_TAG_LENGTH = 4
        private const val NICKNAME_MAX_LENGTH = 10
        private val nicknameRegex = "^[a-zA-Z가-힣0-9 ]+$".toRegex()
    }

    private fun readAndSplitByComma(resource: ClassPathResource): List<String> {
        return resource.inputStream
            .readBytes()
            .decodeToString()
            .split("\n")
    }

    suspend fun generateUniqueRandomNickname(): String {
        val nickname = createRandomNickname()
        return appendNewTag(nickname)
    }

    fun generateRandomNickname(): String {
        val nickname = createRandomNickname()
        val uniqueTag = createTag(emptySet())
        return "$nickname$TAG_DELIMITER$uniqueTag"
    }

    fun getNicknameDto(nickname: String): NicknameDto {
        val (nicknameWithoutTag, tag) = nickname.split(TAG_DELIMITER, limit = 2)
        return NicknameDto(nickname = nicknameWithoutTag, tag = tag)
    }

    suspend fun appendNewTag(nickname: String): String {
        if (!isValidNickname(nickname)) throw InvalidNicknameException

        val tagsWithSameNickname = userRepository.findAllByNicknameStartingWith(nickname)
            .mapNotNull { it.getNicknameTag() }
            .toSet()
        val newTag = createTag(tagsWithSameNickname)

        return "$nickname$TAG_DELIMITER$newTag"
    }

    private fun isValidNickname(nickname: String): Boolean {
        return nickname.length <= NICKNAME_MAX_LENGTH && nickname.matches(nicknameRegex)
    }

    private val nicknames = adjectives
        .flatMap { adj -> nouns.map { "$adj $it" } }
        .filter { it.length <= NICKNAME_MAX_LENGTH }

    private fun createRandomNickname(): String {
        return nicknames.random()
    }

    private fun createTag(existingTags: Set<Int>): String =
        generateSequence { (0..9999).random() }
            .filter { it !in existingTags }
            .first()
            .toString()
            .padStart(NICKNAME_TAG_LENGTH, '0')
}
