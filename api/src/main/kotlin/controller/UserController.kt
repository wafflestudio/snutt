package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.auth.AuthProvider
import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.common.extension.toZonedDateTime
import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.filter.SnuttDefaultApiFilterTarget
import com.wafflestudio.snutt.users.data.User
import com.wafflestudio.snutt.users.dto.AuthProvidersCheckDto
import com.wafflestudio.snutt.users.dto.EmailVerificationResultDto
import com.wafflestudio.snutt.users.dto.LocalLoginRequest
import com.wafflestudio.snutt.users.dto.PasswordChangeRequest
import com.wafflestudio.snutt.users.dto.SendEmailRequest
import com.wafflestudio.snutt.users.dto.SocialLoginRequest
import com.wafflestudio.snutt.users.dto.UserDto
import com.wafflestudio.snutt.users.dto.UserLegacyDto
import com.wafflestudio.snutt.users.dto.UserPatchRequest
import com.wafflestudio.snutt.users.dto.VerificationCodeRequest
import com.wafflestudio.snutt.users.service.UserNicknameService
import com.wafflestudio.snutt.users.service.UserService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping(
    "/v1/user",
    "/user",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/info")
    suspend fun getUserInfo(
        @CurrentUser user: User,
    ): UserLegacyDto =
        UserLegacyDto(
            isAdmin = user.isAdmin,
            regDate = user.regDate.toZonedDateTime(),
            notificationCheckedAt = user.notificationCheckedAt.toZonedDateTime(),
            email = user.email,
            localId = user.credential.localId,
            fbName = user.credential.fbName,
        )

    @DeleteMapping("/account")
    suspend fun deleteAccount(
        @CurrentUser user: User,
    ): OkResponse {
        userService.update(user.copy(active = false))
        return OkResponse()
    }

    @PostMapping("/email/verification")
    suspend fun sendVerificationEmail(
        @CurrentUser user: User,
        @RequestBody request: SendEmailRequest,
    ): OkResponse {
        userService.sendVerificationCode(user, request.email)
        return OkResponse()
    }

    @GetMapping("/email/verification")
    suspend fun getEmailVerification(
        @CurrentUser user: User,
    ) = EmailVerificationResultDto(user.isEmailVerified ?: false)

    @DeleteMapping("/email/verification")
    suspend fun resetEmailVerification(
        @CurrentUser user: User,
    ): EmailVerificationResultDto {
        userService.resetEmailVerification(user)
        return EmailVerificationResultDto(false)
    }

    @PostMapping("/email/verification/code")
    suspend fun confirmEmailVerification(
        @CurrentUser user: User,
        @RequestBody request: VerificationCodeRequest,
    ): EmailVerificationResultDto {
        userService.verifyEmail(user, request.code)
        return EmailVerificationResultDto(true)
    }

    @PostMapping("/password")
    suspend fun attachLocal(
        @CurrentUser user: User,
        @RequestBody body: LocalLoginRequest,
    ) = userService.attachLocal(user, body)

    @PutMapping("/password")
    suspend fun changePassword(
        @CurrentUser user: User,
        @RequestBody body: PasswordChangeRequest,
    ) = userService.changePassword(user, body)

    @PostMapping("/facebook")
    suspend fun attachFacebook(
        @CurrentUser user: User,
        @RequestBody request: SocialLoginRequest,
    ) = userService.attachSocial(user, request, AuthProvider.FACEBOOK)

    @PostMapping("/google")
    suspend fun attachGoogle(
        @CurrentUser user: User,
        @RequestBody request: SocialLoginRequest,
    ) = userService.attachSocial(user, request, AuthProvider.GOOGLE)

    @PostMapping("/kakao")
    suspend fun attachKakao(
        @CurrentUser user: User,
        @RequestBody request: SocialLoginRequest,
    ) = userService.attachSocial(user, request, AuthProvider.KAKAO)

    @PostMapping("/apple")
    suspend fun attachApple(
        @CurrentUser user: User,
        @RequestBody request: SocialLoginRequest,
    ) = userService.attachSocial(user, request, AuthProvider.APPLE)

    @DeleteMapping("/facebook")
    suspend fun detachFacebook(
        @CurrentUser user: User,
    ) = userService.detachSocial(user, AuthProvider.FACEBOOK)

    @DeleteMapping("/google")
    suspend fun detachGoogle(
        @CurrentUser user: User,
    ) = userService.detachSocial(user, AuthProvider.GOOGLE)

    @DeleteMapping("/kakao")
    suspend fun detachKakao(
        @CurrentUser user: User,
    ) = userService.detachSocial(user, AuthProvider.KAKAO)

    @DeleteMapping("/apple")
    suspend fun detachApple(
        @CurrentUser user: User,
    ) = userService.detachSocial(user, AuthProvider.APPLE)
}

@RestController
@SnuttDefaultApiFilterTarget
@RequestMapping(
    "/v1/users",
    "/users",
    produces = [MediaType.APPLICATION_JSON_VALUE],
)
class UsersController(
    private val userService: UserService,
    private val userNicknameService: UserNicknameService,
) {
    @GetMapping("/me")
    suspend fun getUserMe(
        @CurrentUser user: User,
    ): UserDto = buildUserDto(user)

    @PatchMapping("/me")
    suspend fun patchUserInfo(
        @CurrentUser user: User,
        @RequestBody body: UserPatchRequest,
    ): UserDto {
        val updatedUser = userService.patchUserInfo(user.id!!, body)
        return buildUserDto(updatedUser)
    }

    @GetMapping("/me/social_providers", "/me/auth-providers")
    suspend fun checkAuthProviders(
        @CurrentUser user: User,
    ) = AuthProvidersCheckDto(
        local = user.credential.localId != null,
        facebook = user.credential.fbName != null,
        google = user.credential.googleSub != null,
        kakao = user.credential.kakaoSub != null,
        apple = user.credential.appleSub != null,
    )

    private fun buildUserDto(user: User) =
        UserDto(
            id = user.id!!,
            isAdmin = user.isAdmin,
            regDate = user.regDate,
            notificationCheckedAt = user.notificationCheckedAt,
            email = user.email,
            localId = user.credential.localId,
            fbName = user.credential.fbName,
            nickname = userNicknameService.getNicknameDto(user.nickname),
        )
}
