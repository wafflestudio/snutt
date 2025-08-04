package com.wafflestudio.snutt.friend.repository

import com.wafflestudio.snutt.friend.data.Friend
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FriendRepository :
    CoroutineCrudRepository<Friend, String>,
    FriendCustomRepository
