package com.wafflestudio.snu4t.friend.service

import com.wafflestudio.snu4t.common.exception.DuplicateFriendException
import com.wafflestudio.snu4t.common.exception.FriendNotFoundException
import com.wafflestudio.snu4t.common.exception.InvalidFriendException
import com.wafflestudio.snu4t.common.exception.UserNotFoundException
import com.wafflestudio.snu4t.friend.data.Friend
import com.wafflestudio.snu4t.friend.repository.FriendRepository
import com.wafflestudio.snu4t.users.data.User
import com.wafflestudio.snu4t.users.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface FriendService {
    suspend fun getFriendUsers(userId: String, isAccepted: Boolean): List<Pair<Friend, User>>

    suspend fun requestFriend(fromUserId: String, toUserNickname: String)

    suspend fun acceptFriend(friendId: String, toUserId: String)

    suspend fun declineFriend(friendId: String, toUserId: String)

    suspend fun breakFriend(friendId: String, userId: String)
}

@Service
class FriendServiceImpl(
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository,
) : FriendService {
    override suspend fun getFriendUsers(userId: String, isAccepted: Boolean): List<Pair<Friend, User>> {
        val userIdToFriend = friendRepository.findAllFriends(userId, isAccepted).associateBy {
            if (it.fromUserId != userId) it.fromUserId else it.toUserId
        }.ifEmpty { return emptyList() }

        val userIds = userIdToFriend.keys.toList()
        val users = userRepository.findAllByIdInAndActiveTrue(userIds)

        return users.map { user ->
            val friend = userIdToFriend[user.id!!]!!
            friend to user
        }
    }

    override suspend fun requestFriend(fromUserId: String, toUserNickname: String) {
        val user = userRepository.findByNicknameAndActiveTrue(toUserNickname) ?: throw UserNotFoundException
        val toUserId = user.id!!

        if (fromUserId == toUserId) throw InvalidFriendException

        val friend = friendRepository.findByUserPair(fromUserId to toUserId)
        if (friend != null) throw DuplicateFriendException

        friendRepository.save(Friend(fromUserId = fromUserId, toUserId = toUserId))
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
