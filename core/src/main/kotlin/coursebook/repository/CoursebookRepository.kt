package com.wafflestudio.snu4t.coursebook.repository

import com.wafflestudio.snu4t.coursebook.data.Coursebook
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface CoursebookRepository : CoroutineCrudRepository<Coursebook, String>
