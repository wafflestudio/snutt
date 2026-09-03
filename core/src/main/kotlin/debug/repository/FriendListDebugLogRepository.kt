package com.wafflestudio.snutt.debug.repository

import com.wafflestudio.snutt.debug.data.FriendListDebugLog
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FriendListDebugLogRepository : CoroutineCrudRepository<FriendListDebugLog, String>
