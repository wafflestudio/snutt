package com.wafflestudio.snu4t.lectures.repository

import com.wafflestudio.snu4t.lectures.data.Lecture
import org.springframework.data.domain.Page

interface LectureRepositoryCustom {
    suspend fun searchLectures(): Page<Lecture>
}
