package com.wafflestudio.snu4t.coursebook.service

import com.wafflestudio.snu4t.common.enum.Semester
import com.wafflestudio.snu4t.coursebook.data.Coursebook
import com.wafflestudio.snu4t.coursebook.repository.CoursebookRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

interface CoursebookService {
    suspend fun getLatestCoursebook(): Coursebook

    suspend fun getCoursebooks(): List<Coursebook>

    suspend fun getSyllabusUrl(year: Int?, semester: Semester?, courseNumber: String?, lectureNumber: String?): String
}

@Service
class CoursebookServiceImpl(private val coursebookRepository: CoursebookRepository) : CoursebookService {
    override suspend fun getLatestCoursebook(): Coursebook =
        coursebookRepository.findFirstByOrderByYearDescSemesterDesc()

    override suspend fun getCoursebooks(): List<Coursebook> = coursebookRepository.findAllByOrderByYearDescSemesterDesc().toList()
    override suspend fun getSyllabusUrl(year: Int?, semester: Semester?, courseNumber: String?, lectureNumber: String?): String {
        return """
            http://sugang.snu.ac.kr/sugang/cc/cc103.action?openSchyy=${year ?: "undefined"}
            &openShtmFg=${makeOpenShtmFg(semester)}&openDetaShtmFg=${makeOpenDetaShtmFg(semester)}
            &sbjtCd=${courseNumber ?: "undefined"}&ltNo=${lectureNumber ?: "undefined"}&sbjtSubhCd=000
        """.replace("\\s+".toRegex(), "")
    }

    private fun makeOpenShtmFg(semester: Semester?) = when (semester) {
        Semester.SPRING, Semester.SUMMER -> "U000200001"
        Semester.AUTUMN, Semester.WINTER -> "U000200002"
        null -> "undefined"
    }

    private fun makeOpenDetaShtmFg(semester: Semester?) = when (semester) {
        Semester.SPRING, Semester.AUTUMN -> "U000300001"
        Semester.SUMMER, Semester.WINTER -> "U000300002"
        null -> "undefined"
    }
}
