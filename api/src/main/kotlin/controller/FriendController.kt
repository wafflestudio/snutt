package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.dto.ListResponse
import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.friend.dto.FriendRequest
import com.wafflestudio.snutt.friend.dto.FriendRequestLinkResponse
import com.wafflestudio.snutt.friend.dto.FriendResponse
import com.wafflestudio.snutt.friend.dto.FriendState
import com.wafflestudio.snutt.friend.dto.UpdateFriendDisplayNameRequest
import com.wafflestudio.snutt.friend.service.FriendService
import com.wafflestudio.snutt.users.data.User
import com.wafflestudio.snutt.users.service.UserNicknameService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping(
    "/v1/friends",
    "/friends",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class FriendController(
    private val friendService: FriendService,
    private val userNicknameService: UserNicknameService,
) {
    @GetMapping("")
    suspend fun getFriends(
        @CurrentUser user: User,
        @RequestParam state: String,
    ): ListResponse<FriendResponse> {
        val userId = user.id!!
        val friendState = FriendState.from(state) ?: throw IllegalArgumentException("Invalid state")

        val content =
            friendService.getMyFriends(userId, friendState).map { (friend, partner) ->
                val partnerDisplayName = friend.getPartnerDisplayName(userId)
                FriendResponse(
                    id = friend.id!!,
                    userId = partner.id!!,
                    displayName = partnerDisplayName,
                    nickname = userNicknameService.getNicknameDto(partner.nickname),
                    createdAt = friend.createdAt,
                )
            }

        return ListResponse(
            content = content,
            totalCount = content.size,
        )
    }

    @PostMapping("")
    suspend fun requestFriend(
        @CurrentUser user: User,
        @RequestBody body: FriendRequest,
    ) = friendService.requestFriend(user.id!!, body.nickname)

    @PostMapping("/{friendId}/accept")
    suspend fun acceptFriend(
        @CurrentUser user: User,
        @PathVariable friendId: String,
    ) = friendService.acceptFriend(friendId, user.id!!)

    @PostMapping("/{friendId}/decline")
    suspend fun declineFriend(
        @CurrentUser user: User,
        @PathVariable friendId: String,
    ) = friendService.declineFriend(friendId, user.id!!)

    @PatchMapping("/{friendId}/display-name")
    suspend fun updateFriendDisplayName(
        @CurrentUser user: User,
        @PathVariable friendId: String,
        @RequestBody body: UpdateFriendDisplayNameRequest,
    ) = friendService.updateFriendDisplayName(user.id!!, friendId, body.displayName)

    @DeleteMapping("/{friendId}")
    suspend fun breakFriend(
        @CurrentUser user: User,
        @PathVariable friendId: String,
    ) = friendService.breakFriend(friendId, user.id!!)

    @GetMapping("/generate-link")
    suspend fun generateFriendLink(
        @CurrentUser user: User,
    ): FriendRequestLinkResponse {
        val friendRequestToken = friendService.generateFriendRequestLink(user.id!!)
        return FriendRequestLinkResponse(friendRequestToken)
    }

    @PostMapping("/accept-link/{requestToken}")
    suspend fun acceptFriendByLink(
        @CurrentUser user: User,
        @PathVariable requestToken: String,
    ) = friendService
        .acceptFriendByLink(user.id!!, requestToken)
        .let { (friend, partner) ->
            FriendResponse(
                id = friend.id!!,
                userId = partner.id!!,
                displayName = friend.getPartnerDisplayName(user.id!!),
                nickname = userNicknameService.getNicknameDto(partner.nickname),
                createdAt = friend.createdAt,
            )
        }
}
