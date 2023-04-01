package com.wafflestudio.snu4t.common.push.repository

import com.wafflestudio.snu4t.common.push.data.FcmLog
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FcmLogRepository : CoroutineCrudRepository<FcmLog, String>
