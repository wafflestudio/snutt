package com.wafflestudio.snutt.notification

import com.wafflestudio.snutt.common.push.PushClient
import com.wafflestudio.snutt.common.push.dto.PushMessage
import com.wafflestudio.snutt.common.push.dto.TargetedPushMessageWithToken
import com.wafflestudio.snutt.notification.data.PushHistory
import com.wafflestudio.snutt.notification.data.PushPreferenceType
import com.wafflestudio.snutt.notification.data.UserDevice
import com.wafflestudio.snutt.notification.service.DeviceService
import com.wafflestudio.snutt.notification.service.PushHistoryRecorder
import com.wafflestudio.snutt.notification.service.PushPreferenceService
import com.wafflestudio.snutt.notification.service.PushServiceImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class PushServiceImplTest :
    BehaviorSpec({
        val deviceService = mockk<DeviceService>()
        val pushClient = mockk<PushClient>(relaxed = true)
        val pushPreferenceService = mockk<PushPreferenceService>()
        val pushHistoryRecorder = mockk<PushHistoryRecorder>()
        val pushService = PushServiceImpl(deviceService, pushClient, pushPreferenceService, pushHistoryRecorder)

        Given("푸시 설정을 끈 유저와 기기가 없는 유저가 섞인 대상") {
            val message = PushMessage(title = "제목", body = "본문")
            val userToPushMessage = mapOf("u1" to message, "u2" to message, "u3" to message)
            coEvery { pushPreferenceService.isPushPreferenceEnabled(any<List<String>>(), PushPreferenceType.DIARY) } returns
                mapOf("u1" to true, "u2" to true, "u3" to false)
            coEvery { deviceService.getUsersDevices(listOf("u1", "u2")) } returns
                mapOf("u1" to listOf(UserDevice.of("u1", "token-a"), UserDevice.of("u1", "token-b")))
            val sentMessages = slot<List<TargetedPushMessageWithToken>>()
            coEvery { pushClient.sendMessages(capture(sentMessages)) } returns Unit
            val history = slot<PushHistory>()
            every { pushHistoryRecorder.record(capture(history)) } returns Unit

            When("sendTargetPushes를 호출하면") {
                val result = pushService.sendTargetPushes(userToPushMessage, PushPreferenceType.DIARY)

                Then("필터 전/후 유저 수와 토큰 수를 반환하고 기록한다") {
                    result.pushId shouldMatch Regex("diary-\\d{8}-\\d{6}-[a-z0-9]{6}")
                    result.pushType shouldBe "diary"
                    result.targetUserCount shouldBe 3
                    result.sentUserCount shouldBe 1
                    result.sentTokenCount shouldBe 2
                    history.captured.pushId shouldBe result.pushId
                    history.captured.targetUserCount shouldBe 3
                    history.captured.sentUserCount shouldBe 1
                    history.captured.sentTokenCount shouldBe 2
                    history.captured.isGlobal shouldBe false
                    history.captured.title shouldBe "제목"
                }

                Then("모든 메시지에 같은 push_id와 push_type을 data로 싣는다") {
                    sentMessages.captured shouldHaveSize 2
                    sentMessages.captured.forEach {
                        it.message.data.payload[PushMessage.TrackingKeys.PUSH_ID] shouldBe result.pushId
                        it.message.data.payload[PushMessage.TrackingKeys.PUSH_TYPE] shouldBe "diary"
                    }
                }
            }
        }

        Given("전체 푸시") {
            val message = PushMessage(title = "공지", body = "본문", data = mapOf("key" to "value"))
            val sentMessage = slot<PushMessage>()
            coEvery { pushClient.sendGlobalMessage(capture(sentMessage)) } returns Unit
            val history = slot<PushHistory>()
            every { pushHistoryRecorder.record(capture(history)) } returns Unit

            When("sendGlobalPush를 호출하면") {
                pushService.sendGlobalPush(message)

                Then("기존 data를 유지하면서 push_id와 push_type을 싣고 topic 발송으로 기록한다") {
                    coVerify(exactly = 1) { pushClient.sendGlobalMessage(any()) }
                    sentMessage.captured.data.payload["key"] shouldBe "value"
                    sentMessage.captured.data.payload[PushMessage.TrackingKeys.PUSH_TYPE] shouldBe "normal"
                    sentMessage.captured.data.payload[PushMessage.TrackingKeys.PUSH_ID] shouldBe history.captured.pushId
                    history.captured.isGlobal shouldBe true
                    history.captured.topic shouldBe PushClient.GLOBAL_TOPIC
                    history.captured.targetUserCount shouldBe null
                    history.captured.title shouldBe "공지"
                }
            }
        }
    })
