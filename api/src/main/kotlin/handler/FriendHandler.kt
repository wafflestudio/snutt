package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.dto.ListResponse
import com.wafflestudio.snu4t.friend.dto.FriendRequest
import com.wafflestudio.snu4t.friend.dto.FriendResponse
import com.wafflestudio.snu4t.friend.service.FriendService
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody

@Component
class FriendHandler(
    private val friendService: FriendService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun getFriends(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val isAccepted: Boolean = req.parseQueryParam("isAccepted") ?: true

        val content = friendService.getFriendUsers(userId, isAccepted).map { (friend, toUser) ->
            FriendResponse(
                id = friend.id!!,
                userId = toUser.id!!,
                nickname = requireNotNull(toUser.nickname),
                createdAt = friend.createdAt,
            )
        }

        ListResponse(
            content = content,
            totalCount = content.size,
            nextPageToken = null,
        )
    }

    suspend fun requestFriend(req: ServerRequest) = handle(req) {
        val fromUserId = req.userId
        val body = req.awaitBody<FriendRequest>()

        friendService.requestFriend(fromUserId, body.nickname)
    }

    suspend fun acceptFriend(req: ServerRequest) = handle(req) {
        val toUserId = req.userId
        val friendId = req.pathVariable("friendId")

        friendService.acceptFriend(friendId, toUserId)
    }

    suspend fun declineFriend(req: ServerRequest) = handle(req) {
        val toUserId = req.userId
        val friendId = req.pathVariable("friendId")

        friendService.declineFriend(friendId, toUserId)
    }

    suspend fun breakFriend(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val friendId = req.pathVariable("friendId")

        friendService.breakFriend(friendId, userId)
    }
}
