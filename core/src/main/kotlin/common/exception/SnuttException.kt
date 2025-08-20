package com.wafflestudio.snutt.common.exception

import com.wafflestudio.snutt.auth.AuthProvider

open class SnuttException(
    val error: ErrorType = ErrorType.DEFAULT_ERROR,
    val errorMessage: String = error.errorMessage,
    val displayMessage: String = error.displayMessage,
    // TODO: 구버전 대응용 ext 필드. 추후 삭제
    val ext: Map<String, String> = mapOf(),
) : RuntimeException(errorMessage)

object InvalidTimeException : SnuttException(ErrorType.INVALID_TIME)

object InvalidTimetableTitleException : SnuttException(ErrorType.INVALID_TIMETABLE_TITLE)

object WrongApiKeyException : SnuttException(ErrorType.WRONG_API_KEY)

object NoUserTokenException : SnuttException(ErrorType.NO_USER_TOKEN)

object WrongUserTokenException : SnuttException(ErrorType.WRONG_USER_TOKEN)

object UserNotAdminException : SnuttException(ErrorType.USER_NOT_ADMIN)

object WrongLocalIdException : SnuttException(ErrorType.WRONG_LOCAL_ID)

object WrongPasswordException : SnuttException(ErrorType.WRONG_PASSWORD)

object InvalidLocalIdException : SnuttException(ErrorType.INVALID_LOCAL_ID)

object InvalidPasswordException : SnuttException(ErrorType.INVALID_PASSWORD)

object DuplicateLocalIdException : SnuttException(ErrorType.DUPLICATE_LOCAL_ID)

object InvalidEmailException : SnuttException(ErrorType.INVALID_EMAIL)

object UserEmailIsNotVerifiedException : SnuttException(ErrorType.USER_EMAIL_IS_NOT_VERIFIED)

object LectureNotFoundException : SnuttException(ErrorType.LECTURE_NOT_FOUND)

object UserNotFoundException : SnuttException(ErrorType.USER_NOT_FOUND)

object TimetableLectureNotFoundException : SnuttException(ErrorType.TIMETABLE_LECTURE_NOT_FOUND)

class MissingRequiredParameterException(
    fieldName: String,
) : SnuttException(ErrorType.MISSING_PARAMETER, "필수값이 누락되었습니다. ($fieldName)")

class InvalidPathParameterException(
    fieldName: String,
) : SnuttException(ErrorType.INVALID_PARAMETER, "잘못된 값입니다. (path parameter: $fieldName)")

class InvalidQueryParameterException(
    fieldName: String,
) : SnuttException(ErrorType.INVALID_PARAMETER, "잘못된 값입니다. (query parameter: $fieldName)")

class InvalidBodyFieldValueException(
    fieldName: String,
) : SnuttException(ErrorType.INVALID_BODY_FIELD_VALUE, "잘못된 값입니다. (request body: $fieldName)")

object InvalidOsTypeException : SnuttException(ErrorType.INVALID_OS_TYPE)

object InvalidAppTypeException : SnuttException(ErrorType.INVALID_APP_TYPE)

object InvalidNicknameException : SnuttException(ErrorType.INVALID_NICKNAME)

object InvalidDisplayNameException : SnuttException(ErrorType.INVALID_DISPLAY_NAME)

object TableDeleteErrorException : SnuttException(ErrorType.TABLE_DELETE_ERROR)

object InvalidThemeColorCountException : SnuttException(ErrorType.INVALID_THEME_COLOR_COUNT)

object DefaultThemeDeleteErrorException : SnuttException(ErrorType.DEFAULT_THEME_DELETE_ERROR)

object NotDefaultThemeErrorException : SnuttException(ErrorType.NOT_DEFAULT_THEME_ERROR)

object TooManyFilesException : SnuttException(ErrorType.TOO_MANY_FILES)

object EmailAlreadyVerifiedException : SnuttException(ErrorType.EMAIL_ALREADY_VERIFIED)

object TooManyVerificationCodeRequestException : SnuttException(ErrorType.TOO_MANY_VERIFICATION_CODE_REQUEST)

object InvalidVerificationCodeException : SnuttException(ErrorType.INVALID_VERIFICATION_CODE)

object AlreadyLocalAccountException : SnuttException(ErrorType.ALREADY_LOCAL_ACCOUNT)

object AlreadySocialAccountException : SnuttException(ErrorType.ALREADY_SOCIAL_ACCOUNT)

object UpdateAppVersionException : SnuttException(ErrorType.UPDATE_APP_VERSION)

object NotPublishedThemeException : SnuttException(ErrorType.NOT_PUBLISHED_THEME)

object PublishedThemeDeleteErrorException : SnuttException(ErrorType.PUBLISHED_THEME_DELETE_ERROR)

object SocialConnectFailException : SnuttException(ErrorType.SOCIAL_CONNECT_FAIL)

object InvalidAppleLoginTokenException : SnuttException(ErrorType.INVALID_APPLE_LOGIN_TOKEN)

object NoUserFcmKeyException : SnuttException(ErrorType.NO_USER_FCM_KEY)

object InvalidRegistrationForPreviousSemesterCourseException :
    SnuttException(ErrorType.INVALID_REGISTRATION_FOR_PREVIOUS_SEMESTER_COURSE)

object DuplicateTimetableTitleException : SnuttException(ErrorType.DUPLICATE_TIMETABLE_TITLE)

object DuplicateTimetableLectureException : SnuttException(ErrorType.DUPLICATE_LECTURE)

object WrongSemesterException : SnuttException(ErrorType.WRONG_SEMESTER)

class LectureTimeOverlapException(
    confirmMessage: String,
) : SnuttException(
        error = ErrorType.LECTURE_TIME_OVERLAP,
        displayMessage = confirmMessage,
        ext = mapOf("confirm_message" to confirmMessage),
    )

object CustomLectureResetException : SnuttException(ErrorType.CANNOT_RESET_CUSTOM_LECTURE)

object TimetableNotFoundException : SnuttException(ErrorType.TIMETABLE_NOT_FOUND)

object PrimaryTimetableNotFoundException : SnuttException(ErrorType.PRIMARY_TIMETABLE_NOT_FOUND)

object TimetableNotPrimaryException : SnuttException(ErrorType.DEFAULT_ERROR)

object ConfigNotFoundException : SnuttException(ErrorType.CONFIG_NOT_FOUND)

object FriendNotFoundException : SnuttException(ErrorType.FRIEND_NOT_FOUND)

object FriendLinkNotFoundException : SnuttException(ErrorType.FRIEND_LINK_NOT_FOUND)

object SocialProviderNotAttachedException : SnuttException(ErrorType.SOCIAL_PROVIDER_NOT_ATTACHED)

object DiaryQuestionNotFoundException : SnuttException(ErrorType.DIARY_QUESTION_NOT_FOUND)

object DiaryActivityNotFoundException : SnuttException(ErrorType.DIARY_ACTIVITY_NOT_FOUND)

object UserNotFoundByNicknameException : SnuttException(ErrorType.USER_NOT_FOUND_BY_NICKNAME)

object ThemeNotFoundException : SnuttException(ErrorType.THEME_NOT_FOUND)

object EvDataNotFoundException : SnuttException(ErrorType.EV_DATA_NOT_FOUND)

object TagListNotFoundException : SnuttException(ErrorType.TAG_LIST_NOT_FOUND)

object DuplicateVacancyNotificationException : SnuttException(ErrorType.DUPLICATE_VACANCY_NOTIFICATION)

class DuplicateEmailException(
    authProviders: List<AuthProvider>,
) : SnuttException(
        ErrorType.DUPLICATE_EMAIL,
        displayMessage =
            run {
                val socialProviders = authProviders.filter { it != AuthProvider.LOCAL }
                when {
                    socialProviders.isNotEmpty() -> "이미 ${socialProviders.joinToString(", ") { it.korName }}과(와) 연동된 계정이 있습니다."
                    authProviders.contains(AuthProvider.LOCAL) -> "이미 가입된 이메일입니다. 아이디 찾기를 이용해주세요."
                    else -> throw IllegalStateException("로그인 방법이 없는 계정")
                }
            },
    )

object DuplicateFriendException : SnuttException(ErrorType.DUPLICATE_FRIEND)

object InvalidFriendException : SnuttException(ErrorType.INVALID_FRIEND)

object DuplicateThemeNameException : SnuttException(ErrorType.DUPLICATE_THEME_NAME)

object InvalidThemeTypeException : SnuttException(ErrorType.INVALID_THEME_TYPE)

object DuplicatePopupKeyException : SnuttException(ErrorType.DUPLICATE_POPUP_KEY)

object AlreadyDownloadedThemeException : SnuttException(ErrorType.ALREADY_DOWNLOADED_THEME)

object DuplicateSocialAccountException : SnuttException(ErrorType.DUPLICATE_SOCIAL_ACCOUNT)

object CannotRemoveLastAuthProviderException : SnuttException(ErrorType.CANNOT_REMOVE_LAST_AUTH_PROVIDER)

object DynamicLinkGenerationFailedException : SnuttException(ErrorType.DYNAMIC_LINK_GENERATION_FAILED)

object PastSemesterException : SnuttException(ErrorType.PAST_SEMESTER)
