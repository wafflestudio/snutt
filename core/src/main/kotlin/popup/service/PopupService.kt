package com.wafflestudio.snu4t.popup.service

import com.wafflestudio.snu4t.common.client.ClientInfo
import com.wafflestudio.snu4t.common.exception.DuplicatePopupKeyException
import com.wafflestudio.snu4t.popup.data.Popup
import com.wafflestudio.snu4t.popup.dto.PostPopupRequest
import com.wafflestudio.snu4t.popup.repository.PopupRepository
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
        val popup = Popup(
            key = request.key,
            imageOriginUri = request.imageOriginUri,
            hiddenDays = request.hiddenDays,
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
