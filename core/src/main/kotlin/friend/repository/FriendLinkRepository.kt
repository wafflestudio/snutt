package com.wafflestudio.snu4t.friend.repository

import com.wafflestudio.snu4t.friend.data.FriendLink
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface FriendLinkRepository : CoroutineCrudRepository<FriendLink, String>
