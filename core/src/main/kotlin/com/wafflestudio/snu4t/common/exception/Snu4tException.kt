package com.wafflestudio.snu4t.common.exception

open class Snu4tException(
        val error: DefinedError = DefinedError.DEFAULT_ERROR,
        val errorMessage: String = error.errorMessage,
        val displayMessage: String = error.displayMessage,
) : RuntimeException(errorMessage)

object AuthException : Snu4tException(DefinedError.AUTHENTICATION_FAILED)