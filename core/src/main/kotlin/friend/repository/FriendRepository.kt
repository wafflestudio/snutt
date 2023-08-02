package com.wafflestudio.snu4t.friend.repository

import com.wafflestudio.snu4t.friend.data.Friend
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FriendRepository : CoroutineCrudRepository<Friend, String>, FriendCustomRepository
