package com.wafflestudio.snu4t.handler

import com.wafflestudio.snu4t.common.dto.ListResponse
import com.wafflestudio.snu4t.common.dto.OkResponse
import com.wafflestudio.snu4t.friend.dto.FriendRequest
import com.wafflestudio.snu4t.friend.dto.FriendRequestLinkResponse
import com.wafflestudio.snu4t.friend.dto.FriendResponse
import com.wafflestudio.snu4t.friend.dto.FriendState
import com.wafflestudio.snu4t.friend.dto.UpdateFriendDisplayNameRequest
import com.wafflestudio.snu4t.friend.service.FriendService
import com.wafflestudio.snu4t.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snu4t.users.service.UserNicknameService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.server.ServerWebInputException

@Component
class FriendHandler(
    private val friendService: FriendService,
    private val userNicknameService: UserNicknameService,
    snuttRestApiDefaultMiddleware: SnuttRestApiDefaultMiddleware,
) : ServiceHandler(snuttRestApiDefaultMiddleware) {
    suspend fun getFriends(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val state = FriendState.from(req.parseRequiredQueryParam<String>("state")) ?: throw ServerWebInputException("Invalid state")

        val content = friendService.getMyFriends(userId, state).map { (friend, partner) ->
            val partnerDisplayName = friend.getPartnerDisplayName(userId)
            FriendResponse(
                id = friend.id!!,
                userId = partner.id!!,
                displayName = partnerDisplayName,
                nickname = userNicknameService.getNicknameDto(partner.nickname!!),
                createdAt = friend.createdAt,
            )
        }

        ListResponse(
            content = content,
            totalCount = content.size,
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

    suspend fun updateFriendDisplayName(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val friendId = req.pathVariable("friendId")
        val body = req.awaitBody<UpdateFriendDisplayNameRequest>()

        friendService.updateFriendDisplayName(userId, friendId, body.displayName)
    }

    suspend fun breakFriend(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val friendId = req.pathVariable("friendId")

        friendService.breakFriend(friendId, userId)
    }

    suspend fun generateFriendLink(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val friendRequestLink = friendService.generateFriendRequestLink(userId)

        FriendRequestLinkResponse(friendRequestLink.first, friendRequestLink.second)
    }

    suspend fun acceptFriendByLink(req: ServerRequest) = handle(req) {
        val userId = req.userId
        val requestInfo = req.pathVariable("requestInfo")

        friendService.acceptFriendByLink(userId, requestInfo)
        OkResponse()
    }
}
