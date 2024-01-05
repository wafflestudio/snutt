package com.wafflestudio.snu4t.friend.repository

import com.wafflestudio.snu4t.common.extension.desc
import com.wafflestudio.snu4t.friend.data.Friend
import com.wafflestudio.snu4t.friend.dto.FriendState
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo

interface FriendCustomRepository {
    suspend fun findAllFriends(userId: String, state: FriendState): List<Friend>

    suspend fun findByUserPair(userIds: Pair<String, String>): Friend?
}

class FriendCustomRepositoryImpl(
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) : FriendCustomRepository {
    override suspend fun findAllFriends(userId: String, state: FriendState): List<Friend> {
        return reactiveMongoTemplate.find<Friend>(
            Query.query(
                when (state) {
                    FriendState.ACTIVE -> {
                        Criteria().andOperator(
                            Criteria().orOperator(
                                Friend::fromUserId isEqualTo userId,
                                Friend::toUserId isEqualTo userId,
                            ),
                            Friend::isAccepted isEqualTo true,
                        )
                    }
                    FriendState.REQUESTING -> {
                        Criteria().andOperator(
                            Friend::fromUserId isEqualTo userId,
                            Friend::isAccepted isEqualTo false,
                        )
                    }
                    FriendState.REQUESTED -> {
                        Criteria().andOperator(
                            Friend::toUserId isEqualTo userId,
                            Friend::isAccepted isEqualTo false,
                        )
                    }
                }
            ).with(Friend::createdAt.desc())
        ).asFlow().toList()
    }

    override suspend fun findByUserPair(userIds: Pair<String, String>): Friend? {
        return reactiveMongoTemplate.find<Friend>(
            Query.query(
                Criteria().orOperator(
                    Criteria().andOperator(
                        Friend::fromUserId isEqualTo userIds.first,
                        Friend::toUserId isEqualTo userIds.second,
                    ),
                    Criteria().andOperator(
                        Friend::fromUserId isEqualTo userIds.second,
                        Friend::toUserId isEqualTo userIds.first,
                    ),
                )
            )
        ).awaitFirstOrNull()
    }
}
