package com.wafflestudio.snu4t.friend.service

import com.wafflestudio.snu4t.common.exception.DuplicateFriendException
import com.wafflestudio.snu4t.common.exception.FriendNotFoundException
import com.wafflestudio.snu4t.common.exception.InvalidFriendException
import com.wafflestudio.snu4t.common.exception.UserNotFoundException
import com.wafflestudio.snu4t.common.push.dto.PushMessage
import com.wafflestudio.snu4t.friend.data.Friend
import com.wafflestudio.snu4t.friend.dto.FriendState
import com.wafflestudio.snu4t.friend.repository.FriendRepository
import com.wafflestudio.snu4t.notification.data.NotificationType
import com.wafflestudio.snu4t.notification.service.PushWithNotificationService
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface FriendService {
    suspend fun getFriendUsers(userId: String, state: FriendState): List<Pair<Friend, User>>

    suspend fun requestFriend(fromUserId: String, toUserNickname: String)

    suspend fun acceptFriend(friendId: String, toUserId: String)

    suspend fun declineFriend(friendId: String, toUserId: String)

    suspend fun breakFriend(friendId: String, userId: String)
}

@Service
class FriendServiceImpl(
    private val pushWithNotificationService: PushWithNotificationService,
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository,
) : FriendService {
    companion object {
        private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    override suspend fun getFriendUsers(userId: String, state: FriendState): List<Pair<Friend, User>> {
        val userIdToFriend = friendRepository.findAllFriends(userId, state).associateBy {
            if (it.fromUserId != userId) it.fromUserId else it.toUserId
        }.ifEmpty { return emptyList() }

        val userIds = userIdToFriend.keys.toList()
        val users = userRepository.findAllByIdInAndActiveTrue(userIds)

        return users.map { user ->
            val friend = userIdToFriend[user.id!!]!!
            friend to user
        }
    }

    override suspend fun requestFriend(fromUserId: String, toUserNickname: String): Unit = coroutineScope {
        val toUser = userRepository.findByNicknameAndActiveTrue(toUserNickname) ?: throw UserNotFoundException
        val toUserId = toUser.id!!

        if (fromUserId == toUserId) throw InvalidFriendException

        val friend = friendRepository.findByUserPair(fromUserId to toUserId)
        if (friend != null) throw DuplicateFriendException

        friendRepository.save(Friend(fromUserId = fromUserId, toUserId = toUserId))

        coroutineScope.launch {
            val fromUser = requireNotNull(userRepository.findByIdAndActiveTrue(fromUserId))
            sendFriendRequestPush(fromUser, toUser)
        }
    }

    private suspend fun sendFriendRequestPush(fromUser: User, toUser: User) {
        val fromUserNickname = requireNotNull(fromUser.nickname)
        val pushMessage = PushMessage(
            title = "$fromUserNickname 님이 친구 요청을 보냈어요",
            body = "$fromUserNickname 님과 친구하면 서로 대표 시간표를 볼 수 있어요",
        )
        pushWithNotificationService.sendPushAndNotification(pushMessage, NotificationType.NORMAL, toUser.id!!)
    }

    override suspend fun acceptFriend(friendId: String, toUserId: String) {
        val friend = friendRepository.findById(friendId) ?: throw FriendNotFoundException
        if (friend.toUserId != toUserId || friend.isAccepted) throw FriendNotFoundException

        friend.isAccepted = true
        friend.updatedAt = LocalDateTime.now()
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
}
