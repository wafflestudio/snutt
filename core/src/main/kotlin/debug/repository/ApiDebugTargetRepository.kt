package com.wafflestudio.snutt.debug.repository

import com.wafflestudio.snutt.debug.data.ApiDebugTarget
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ApiDebugTargetRepository : CoroutineCrudRepository<ApiDebugTarget, String>
