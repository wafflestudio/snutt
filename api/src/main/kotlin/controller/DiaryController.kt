package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.client.AppVersion
import com.wafflestudio.snutt.common.client.ClientInfo
import com.wafflestudio.snutt.common.client.OsType
import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.common.enums.Semester
import com.wafflestudio.snutt.common.exception.DiaryTargetLectureNotFoundException
import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.diary.dto.DiaryDailyClassTypeDto
import com.wafflestudio.snutt.diary.dto.DiaryQuestionnaireDto
import com.wafflestudio.snutt.diary.dto.DiarySubmissionSummaryDto
import com.wafflestudio.snutt.diary.dto.DiarySubmissionsOfYearSemesterDto
import com.wafflestudio.snutt.diary.dto.DiaryTargetLectureDto
import com.wafflestudio.snutt.diary.dto.request.DiaryQuestionnaireRequestDto
import com.wafflestudio.snutt.diary.dto.request.DiarySubmissionRequestDto
import com.wafflestudio.snutt.diary.service.DiaryService
import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.users.data.User
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val ANDROID_LEGACY_DATE_MAX_VERSION = AppVersion("3.12.4")

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping(
    "/v1/diary",
    "/diary",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class DiaryController(
    private val diaryService: DiaryService,
) {
    @PostMapping("/questionnaire")
    suspend fun getQuestionnaireFromDailyClassTypes(
        @CurrentUser user: User,
        @RequestBody body: DiaryQuestionnaireRequestDto,
    ) = DiaryQuestionnaireDto(
        diaryService.generateQuestionnaire(user.id!!, body.lectureId, body.dailyClassTypes),
    )

    @GetMapping("/target")
    suspend fun getRandomTargetLecture(
        @CurrentUser user: User,
        @RequestParam year: Int,
        @RequestParam semester: Semester,
    ): DiaryTargetLectureDto {
        val targetLecture =
            diaryService.getDiaryTargetLecture(user.id!!, year, semester, listOf()) ?: throw DiaryTargetLectureNotFoundException
        return DiaryTargetLectureDto(targetLecture)
    }

    @GetMapping("/my")
    suspend fun getMySubmissions(
        @CurrentUser user: User,
        @RequestAttribute("clientInfo") clientInfo: ClientInfo,
    ): List<DiarySubmissionsOfYearSemesterDto> {
        val submissions = diaryService.getMySubmissions(user.id!!)
        val submissionIdShortQuestionRepliesMap = diaryService.getSubmissionIdShortQuestionRepliesMap(submissions)
        val appVersion = clientInfo.appVersion
        val useLegacyDateFormat =
            clientInfo.osType == OsType.ANDROID &&
                appVersion != null &&
                appVersion <= ANDROID_LEGACY_DATE_MAX_VERSION

        return submissions
            .groupBy { submission ->
                submission.year to submission.semester
            }.map {
                DiarySubmissionsOfYearSemesterDto(
                    year = it.key.first,
                    semester = it.key.second.value,
                    submissions =
                        it.value.map { submission ->
                            DiarySubmissionSummaryDto(submission, submissionIdShortQuestionRepliesMap[submission.id]!!, useLegacyDateFormat)
                        },
                )
            }.sortedWith(compareByDescending<DiarySubmissionsOfYearSemesterDto> { it.year }.thenByDescending { it.semester })
    }

    @GetMapping("/dailyClassTypes")
    suspend fun getDailyClassTypes() =
        diaryService.getActiveDailyClassTypes().map {
            DiaryDailyClassTypeDto(it)
        }

    @PostMapping("")
    suspend fun submitDiary(
        @CurrentUser user: User,
        @RequestBody body: DiarySubmissionRequestDto,
    ): OkResponse {
        diaryService.submitDiary(user.id!!, body)
        return OkResponse()
    }

    @DeleteMapping("/{id}")
    suspend fun removeDiarySubmission(
        @CurrentUser user: User,
        @PathVariable id: String,
    ): OkResponse {
        diaryService.removeSubmission(id, user.id!!)
        return OkResponse()
    }
}
