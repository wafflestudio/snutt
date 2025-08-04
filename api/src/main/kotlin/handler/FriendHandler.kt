package com.wafflestudio.snutt.handler

import com.wafflestudio.snutt.common.dto.ListResponse
import com.wafflestudio.snutt.friend.dto.FriendRequest
import com.wafflestudio.snutt.friend.dto.FriendRequestLinkResponse
import com.wafflestudio.snutt.friend.dto.FriendResponse
import com.wafflestudio.snutt.friend.dto.FriendState
import com.wafflestudio.snutt.friend.dto.UpdateFriendDisplayNameRequest
import com.wafflestudio.snutt.friend.service.FriendService
import com.wafflestudio.snutt.middleware.SnuttRestApiDefaultMiddleware
import com.wafflestudio.snutt.users.service.UserNicknameService
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
    suspend fun getFriends(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val state = FriendState.from(req.parseRequiredQueryParam<String>("state")) ?: throw ServerWebInputException("Invalid state")

            val content =
                friendService.getMyFriends(userId, state).map { (friend, partner) ->
                    val partnerDisplayName = friend.getPartnerDisplayName(userId)
                    FriendResponse(
                        id = friend.id!!,
                        userId = partner.id!!,
                        displayName = partnerDisplayName,
                        nickname = userNicknameService.getNicknameDto(partner.nickname),
                        createdAt = friend.createdAt,
                    )
                }

            ListResponse(
                content = content,
                totalCount = content.size,
            )
        }

    suspend fun requestFriend(req: ServerRequest) =
        handle(req) {
            val fromUserId = req.userId
            val body = req.awaitBody<FriendRequest>()

            friendService.requestFriend(fromUserId, body.nickname)
        }

    suspend fun acceptFriend(req: ServerRequest) =
        handle(req) {
            val toUserId = req.userId
            val friendId = req.pathVariable("friendId")

            friendService.acceptFriend(friendId, toUserId)
        }

    suspend fun declineFriend(req: ServerRequest) =
        handle(req) {
            val toUserId = req.userId
            val friendId = req.pathVariable("friendId")

            friendService.declineFriend(friendId, toUserId)
        }

    suspend fun updateFriendDisplayName(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val friendId = req.pathVariable("friendId")
            val body = req.awaitBody<UpdateFriendDisplayNameRequest>()

            friendService.updateFriendDisplayName(userId, friendId, body.displayName)
        }

    suspend fun breakFriend(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val friendId = req.pathVariable("friendId")

            friendService.breakFriend(friendId, userId)
        }

    suspend fun generateFriendLink(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val friendRequestToken = friendService.generateFriendRequestLink(userId)

            FriendRequestLinkResponse(friendRequestToken)
        }

    suspend fun acceptFriendByLink(req: ServerRequest) =
        handle(req) {
            val userId = req.userId
            val requestToken = req.pathVariable("requestToken")

            friendService
                .acceptFriendByLink(userId, requestToken)
                .let { (friend, partner) ->
                    FriendResponse(
                        id = friend.id!!,
                        userId = partner.id!!,
                        displayName = friend.getPartnerDisplayName(userId),
                        nickname = userNicknameService.getNicknameDto(partner.nickname),
                        createdAt = friend.createdAt,
                    )
                }
        }
}
