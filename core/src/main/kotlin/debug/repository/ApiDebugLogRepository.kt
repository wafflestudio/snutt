package com.wafflestudio.snutt.debug.repository

import com.wafflestudio.snutt.debug.data.ApiDebugLog
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ApiDebugLogRepository : CoroutineCrudRepository<ApiDebugLog, String>
