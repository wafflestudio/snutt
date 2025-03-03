package com.wafflestudio.snutt.popup.service

import com.wafflestudio.snutt.common.client.ClientInfo
import com.wafflestudio.snutt.common.exception.DuplicatePopupKeyException
import com.wafflestudio.snutt.popup.data.Popup
import com.wafflestudio.snutt.popup.dto.PostPopupRequest
import com.wafflestudio.snutt.popup.repository.PopupRepository
import kotlinx.coroutines.flow.toList
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service

interface PopupService {
    suspend fun postPopup(request: PostPopupRequest): Popup

    suspend fun deletePopup(popupId: String)

    suspend fun getPopups(clientInfo: ClientInfo): List<Popup>
}

@Service
class PopupServiceImpl(
    private val popupRepository: PopupRepository,
) : PopupService {
    override suspend fun postPopup(request: PostPopupRequest): Popup {
        val popup =
            Popup(
                key = request.key,
                imageOriginUri = request.imageOriginUri,
                hiddenDays = request.hiddenDays,
                linkUrl = request.linkUrl,
            )
        return try {
            popupRepository.save(popup)
        } catch (e: DuplicateKeyException) {
            throw DuplicatePopupKeyException
        }
    }

    override suspend fun deletePopup(popupId: String) {
        popupRepository.deleteById(popupId)
    }

    override suspend fun getPopups(clientInfo: ClientInfo): List<Popup> {
        return popupRepository.findAll().toList().sortedBy { it.createdAt }
    }
}
