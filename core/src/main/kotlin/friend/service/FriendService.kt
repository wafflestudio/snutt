package com.wafflestudio.snu4t.friend.service

import com.wafflestudio.snu4t.common.exception.DuplicateFriendException
import com.wafflestudio.snu4t.common.exception.FriendNotFoundException
import com.wafflestudio.snu4t.common.exception.InvalidDisplayNameException
import com.wafflestudio.snu4t.common.exception.InvalidFriendException
import com.wafflestudio.snu4t.common.exception.UserNotFoundByNicknameException
import com.wafflestudio.snu4t.common.push.UrlScheme
import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.friend.data.Friend
import com.wafflestudio.snu4t.friend.dto.FriendState
import com.wafflestudio.snu4t.friend.repository.FriendRepository
import com.wafflestudio.snu4t.notification.data.NotificationType
import com.wafflestudio.snu4t.notification.service.PushWithNotificationService
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.repository.UserRepository
import com.wafflestudio.snu4t.users.service.UserNicknameService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface FriendService {
    suspend fun getMyFriends(myUserId: String, state: FriendState): List<Pair<Friend, User>>

    suspend fun requestFriend(fromUserId: String, toUserNickname: String)

    suspend fun acceptFriend(friendId: String, toUserId: String)

    suspend fun updateFriendDisplayName(userId: String, friendId: String, displayName: String)

    suspend fun declineFriend(friendId: String, toUserId: String)

    suspend fun breakFriend(friendId: String, userId: String)

    suspend fun get(friendId: String): Friend?
}

@Service
class FriendServiceImpl(
    private val pushWithNotificationService: PushWithNotificationService,
    private val userNicknameService: UserNicknameService,
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository,
) : FriendService {
    companion object {
        private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        private val friendDisplayNameRegex = "^[a-zA-Z가-힣0-9 ]+$".toRegex()
        private const val DISPLAY_NAME_MAX_LENGTH = 10
    }

    override suspend fun getMyFriends(myUserId: String, state: FriendState): List<Pair<Friend, User>> {
        val userIdToFriend = friendRepository.findAllFriends(myUserId, state)
            .associateBy { it.getPartnerUserId(myUserId) }
            .ifEmpty { return emptyList() }

        val userIds = userIdToFriend.keys.toList()
        val users = userRepository.findAllByIdInAndActiveTrue(userIds)

        return users.map { user ->
            val friend = userIdToFriend[user.id!!]!!
            friend to user
        }
    }

    override suspend fun requestFriend(fromUserId: String, toUserNickname: String): Unit = coroutineScope {
        val toUser = userRepository.findByNicknameAndActiveTrue(toUserNickname) ?: throw UserNotFoundByNicknameException
        val toUserId = toUser.id!!

        if (fromUserId == toUserId) throw InvalidFriendException

        val friend = friendRepository.findByUserPair(fromUserId to toUserId)
        if (friend != null) throw DuplicateFriendException

        friendRepository.save(Friend(fromUserId = fromUserId, toUserId = toUserId))

        coroutineScope.launch {
            val fromUser = requireNotNull(userRepository.findByIdAndActiveTrue(fromUserId))
            sendFriendRequestPush(fromUser, toUser.id)
        }
    }

    private suspend fun sendFriendRequestPush(fromUser: User, toUserId: String) {
        val fromUserNickname = userNicknameService.getNicknameDto(fromUser.nickname!!).nickname
        val pushMessage = PushMessage(
            title = "친구 요청",
            body = "'$fromUserNickname'님의 친구 요청을 수락하고 서로의 대표 시간표를 확인해보세요!",
            urlScheme = UrlScheme.FRIENDS,
        )
        pushWithNotificationService.sendPushAndNotification(pushMessage, NotificationType.FRIEND_REQUEST, toUserId)
    }

    override suspend fun acceptFriend(friendId: String, toUserId: String) {
        val friend = friendRepository.findById(friendId) ?: throw FriendNotFoundException
        if (friend.toUserId != toUserId || friend.isAccepted) throw FriendNotFoundException

        friend.isAccepted = true
        friend.updatedAt = LocalDateTime.now()
        friendRepository.save(friend)

        coroutineScope.launch {
            val toUser = requireNotNull(userRepository.findByIdAndActiveTrue(friend.toUserId))
            sendFriendAcceptPush(friend.fromUserId, toUser)
        }
    }

    private suspend fun sendFriendAcceptPush(fromUserId: String, toUser: User) {
        val toUserNickname = userNicknameService.getNicknameDto(toUser.nickname!!).nickname
        val pushMessage = PushMessage(
            title = "친구 요청 수락",
            body = "'$toUserNickname'님과 친구가 되었어요.",
            urlScheme = UrlScheme.FRIENDS,
        )
        pushWithNotificationService.sendPushAndNotification(pushMessage, NotificationType.FRIEND_REQUEST_ACCEPT, fromUserId)
    }

    override suspend fun updateFriendDisplayName(userId: String, friendId: String, displayName: String) {
        val friend = friendRepository.findById(friendId) ?: throw FriendNotFoundException
        if ((friend.fromUserId != userId && friend.toUserId != userId) || !friend.isAccepted) throw FriendNotFoundException

        val validDisplayName =
            displayName.length <= DISPLAY_NAME_MAX_LENGTH &&
                displayName.matches(friendDisplayNameRegex)

        if (!validDisplayName) throw InvalidDisplayNameException

        friend.updatePartnerDisplayName(userId, displayName)
        friendRepository.save(friend)
    }

    override suspend fun declineFriend(friendId: String, toUserId: String) {
        val friend = friendRepository.findById(friendId) ?: throw FriendNotFoundException
        if (friend.toUserId != toUserId || friend.isAccepted) throw FriendNotFoundException

        friendRepository.delete(friend)
    }

    override suspend fun breakFriend(friendId: String, userId: String) {
        val friend = friendRepository.findById(friendId) ?: throw FriendNotFoundException
        if ((friend.fromUserId != userId && friend.toUserId != userId) || !friend.isAccepted) throw FriendNotFoundException

        friendRepository.delete(friend)
    }

    override suspend fun get(friendId: String): Friend? {
        return friendRepository.findById(friendId)
    }
}
