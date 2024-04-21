package com.wafflestudio.snu4t.friend.repository

import com.wafflestudio.snu4t.friend.data.FriendRequestLink
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FriendRequestLinkRepository : CoroutineCrudRepository<FriendRequestLink, String> {
    fun findByEncodedString(encodedString: String): Flow<FriendRequestLink?>
}
