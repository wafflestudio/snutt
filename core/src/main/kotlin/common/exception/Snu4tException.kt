package com.wafflestudio.snu4t.common.exception

open class Snu4tException(
    val error: ErrorType = ErrorType.DEFAULT_ERROR,
    val errorMessage: String = error.errorMessage,
    val displayMessage: String = error.displayMessage,
) : RuntimeException(errorMessage)

object WrongApiKeyException : Snu4tException(ErrorType.WRONG_API_KEY)
object NoUserTokenException : Snu4tException(ErrorType.NO_USER_TOKEN)
object UserNotAdminException : Snu4tException(ErrorType.USER_NOT_ADMIN)
object NoUserFcmKeyException : Snu4tException(ErrorType.NO_USER_FCM_KEY)
object WrongUserTokenException : Snu4tException(ErrorType.WRONG_USER_TOKEN)
object InvalidLocalIdException : Snu4tException(ErrorType.INVALID_LOCAL_ID)
object InvalidPasswordException : Snu4tException(ErrorType.INVALID_PASSWORD)
object InvalidEmailException : Snu4tException(ErrorType.INVALID_EMAIL)
object DuplicateLocalIdException : Snu4tException(ErrorType.DUPLICATE_LOCAL_ID)

object LectureNotFoundException : Snu4tException(ErrorType.LECTURE_NOT_FOUND)

object TimetableNotFoundException : Snu4tException(ErrorType.TIMETABLE_NOT_FOUND)
object SharedTimetableNotFoundException : Snu4tException(ErrorType.SHARED_TIMETABLE_NOT_FOUND)
object NotSharedTimetableOwnerException : Snu4tException(ErrorType.NOT_SHARED_TIMETABLE_OWNER)
object SharedTimetableAlreadyExistsException : Snu4tException(ErrorType.SHARED_TIME_TABLE_ALREADY_EXISTS)
class MissingRequiredParameterException(private val fieldName: String) :
    Snu4tException(ErrorType.MISSING_PARAMETER, "필수값이 누락되었습니다. ($fieldName)")

class InvalidParameterException(private val fieldName: String) :
    Snu4tException(ErrorType.INVALID_PARAMETER, "잘못된 값입니다. (query parameter: $fieldName)")

class InvalidBodyFieldValueException(private val fieldName: String) :
    Snu4tException(ErrorType.INVALID_BODY_FIELD_VALUE, "잘못된 값입니다. (request body: $fieldName)")
