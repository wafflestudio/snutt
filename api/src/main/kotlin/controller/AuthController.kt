package com.wafflestudio.snutt.controller

import com.wafflestudio.snutt.common.dto.OkResponse
import com.wafflestudio.snutt.config.CurrentUser
import com.wafflestudio.snutt.filter.SnuttNoAuthApiFilterTarget
import com.wafflestudio.snutt.users.data.User
import com.wafflestudio.snutt.users.dto.EmailResponse
import com.wafflestudio.snutt.users.dto.FacebookLoginRequest
import com.wafflestudio.snutt.users.dto.GetMaskedEmailRequest
import com.wafflestudio.snutt.users.dto.LocalLoginRequest
import com.wafflestudio.snutt.users.dto.LocalRegisterRequest
import com.wafflestudio.snutt.users.dto.LogoutRequest
import com.wafflestudio.snutt.users.dto.PasswordResetRequest
import com.wafflestudio.snutt.users.dto.SendEmailRequest
import com.wafflestudio.snutt.users.dto.SocialLoginRequest
import com.wafflestudio.snutt.users.dto.VerificationCodeRequest
import com.wafflestudio.snutt.users.service.UserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SnuttNoAuthApiFilterTarget
@RequestMapping("/v1/auth", "/auth")
class AuthController(
    private val userService: UserService,
) {
    @PostMapping("/register_local")
    suspend fun registerLocal(
        @RequestBody request: LocalRegisterRequest,
    ) = userService.registerLocal(request)

    @PostMapping("/login_local")
    suspend fun loginLocal(
        @RequestBody request: LocalLoginRequest,
    ) = userService.loginLocal(request)

    @PostMapping("/login_fb")
    suspend fun loginFacebookLegacy(
        @RequestBody request: FacebookLoginRequest,
    ) = userService.loginFacebook(SocialLoginRequest(request.fbToken))

    @PostMapping("/login/facebook")
    suspend fun loginFacebook(
        @RequestBody request: SocialLoginRequest,
    ) = userService.loginFacebook(request)

    @PostMapping("/login/google")
    suspend fun loginGoogle(
        @RequestBody request: SocialLoginRequest,
    ) = userService.loginGoogle(request)

    @PostMapping("/login/kakao")
    suspend fun loginKakao(
        @RequestBody request: SocialLoginRequest,
    ) = userService.loginKakao(request)

    @PostMapping("/login_apple")
    suspend fun loginAppleLegacy(
        @RequestBody request: SocialLoginRequest,
    ) = userService.loginApple(request)

    @PostMapping("/login/apple")
    suspend fun loginApple(
        @RequestBody request: SocialLoginRequest,
    ) = userService.loginApple(request)

    @PostMapping("/id/find")
    suspend fun findId(
        @RequestBody request: SendEmailRequest,
    ): OkResponse {
        userService.sendLocalIdToEmail(request.email)
        return OkResponse()
    }

    @PostMapping("/password/reset/email/send")
    suspend fun sendResetPasswordCode(
        @RequestBody request: SendEmailRequest,
    ): OkResponse {
        userService.sendResetPasswordCode(request.email)
        return OkResponse()
    }

    @PostMapping("/password/reset/verification/code")
    suspend fun verifyResetPasswordCode(
        @RequestBody body: VerificationCodeRequest,
    ): OkResponse {
        userService.verifyResetPasswordCode(body.userId!!, body.code)
        return OkResponse()
    }

    @PostMapping("/password/reset/email/check")
    suspend fun getMaskedEmail(
        @RequestBody request: GetMaskedEmailRequest,
    ) = EmailResponse(userService.getMaskedEmail(request.userId))

    @PostMapping("/password/reset")
    suspend fun resetPassword(
        @RequestBody body: PasswordResetRequest,
    ): OkResponse {
        userService.resetPassword(body.userId, body.password, body.code)
        return OkResponse()
    }

    @PostMapping("/logout")
    suspend fun logout(
        @CurrentUser user: User,
        @RequestBody request: LogoutRequest,
    ): OkResponse {
        userService.logout(user, request)
        return OkResponse()
    }
}
